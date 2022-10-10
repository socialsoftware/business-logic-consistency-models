package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.DomainEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEvents;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEventsRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.*;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

@Service
public class TournamentService {
    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private QuizService quizService;

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private ProcessedEventsRepository processedEventsRepository;

    @Autowired
    private EventRepository eventRepository;

    // intended for requests from external functionalities
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TournamentDto getCausalTournamentRemote(Integer aggregateId, UnitOfWork unitOfWorkWorkService) {
        return new TournamentDto(getCausalTournamentLocal(aggregateId, unitOfWorkWorkService));
    }

    // intended for requests from local functionalities

    public Tournament getCausalTournamentLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        Tournament tournament = tournamentRepository.findCausal(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, aggregateId));

        if(tournament.getState() == DELETED) {
            throw new TutorException(TOURNAMENT_DELETED, tournament.getAggregateId());
        }

        Set<DomainEvent> allEvents = new HashSet<>(eventRepository.findAll());
        Set<ProcessedEvents> tournamentProcessedEvents = new HashSet<>(processedEventsRepository.findAll());


        unitOfWork.addToCausalSnapshot(tournament, allEvents, tournamentProcessedEvents);
        return tournament;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<TournamentDto> getAllCausalCourseExecutions(UnitOfWork unitOfWork) {
        return tournamentRepository.findAllActive().stream()
                .map(Tournament::getAggregateId)
                .distinct()
                .map(id -> getCausalTournamentLocal(id, unitOfWork))
                .map(TournamentDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TournamentDto createTournament(TournamentDto tournamentDto, TournamentCreator creator,
                                          TournamentCourseExecution courseExecution, Set<TournamentTopic> topics,
                                          TournamentQuiz quiz, UnitOfWork unitOfWorkWorkService) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        if(creator.getName().equals("ANONYMOUS") || creator.getUsername().equals("ANONYMOUS")) {
            throw new TutorException(ErrorMessage.USER_IS_ANONYMOUS, creator.getAggregateId());
        }
        Tournament tournament = new Tournament(aggregateId, tournamentDto, creator, courseExecution, topics, quiz); /* should the skeleton creation be part of the functionality?? */
        tournament.setPrimaryAggregate(true);
        unitOfWorkWorkService.addAggregateToCommit(tournament);
        //unitOfWorkWorkService.addEvent(new TournamentCreationEvent(tournament));
        return new TournamentDto(tournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void addParticipant(Integer tournamentAggregateId, TournamentParticipant tournamentParticipant, UnitOfWork unitOfWorkWorkService) {
        if(tournamentParticipant.getName().equals("ANONYMOUS") || tournamentParticipant.getUsername().equals("ANONYMOUS")) {
            throw new TutorException(ErrorMessage.USER_IS_ANONYMOUS, tournamentParticipant.getAggregateId());
        }
        Tournament tournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWorkWorkService);
        Tournament newTournamentVersion = new Tournament(tournament);
        newTournamentVersion.addParticipant(tournamentParticipant);
        unitOfWorkWorkService.addAggregateToCommit(newTournamentVersion);
    }

    /*TODO refactor this*/

    /* discuss if the processing for all tournaments should be done at the same time */
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void anonymizeUser(Integer tournamentAggregateId, Integer userAggregateId, String name, String username, Integer eventVersion, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        //these if's are necessary if the aggregate already through direct access has a more recent version and it will still count as processed
        if(oldTournament.getCreator().getAggregateId().equals(userAggregateId) && oldTournament.getCreator().getVersion() >= eventVersion) {
            return;
        }

        TournamentParticipant oldParticipant = oldTournament.findParticipant(userAggregateId);
        if(oldParticipant != null && oldParticipant.getAggregateId().equals(userAggregateId) && oldParticipant.getVersion() >= tournamentAggregateId) {
            return;
        }

        Tournament newTournament = new Tournament(oldTournament);

        if(newTournament.getCreator().getAggregateId().equals(userAggregateId)) {
            newTournament.getCreator().setName(name);
            newTournament.getCreator().setUsername(username);
            newTournament.getCreator().setVersion(eventVersion);
            unitOfWork.addAggregateToCommit(newTournament);
        }

        /*TournamentParticipant participantToAnonymize = newTournament.findParticipant(userAggregateId);
        participantToAnonymize.setName(name);
        participantToAnonymize.setUsername(name);*/

        for(TournamentParticipant tp : newTournament.getParticipants()) {
            if(tp.getAggregateId().equals(userAggregateId)) {
                tp.setName(name);
                tp.setUsername(username);
                tp.setVersion(eventVersion);
                unitOfWork.addAggregateToCommit(newTournament);
            }
        }
    }

    private Set<Tournament> findAllTournamentByVersion(UnitOfWork unitOfWork) {
        Set<Tournament> tournaments = tournamentRepository.findAll()
                .stream()
                .filter(t -> t.getVersion() < unitOfWork.getVersion())
                .collect(Collectors.toSet());

        Map<Integer, Tournament> tournamentPerAggregateId = new HashMap<>();
        for(Tournament t : tournaments) {
            if(t.getState() == DELETED) {
                throw new TutorException(TOURNAMENT_DELETED, t.getAggregateId());
            }

            Set<DomainEvent> allEvents = new HashSet<>(eventRepository.findAll());
            Set<ProcessedEvents> tournamentProcessedEvents = new HashSet<>(processedEventsRepository.findAll());

            unitOfWork.addToCausalSnapshot(t, allEvents, tournamentProcessedEvents);

            if (!tournamentPerAggregateId.containsKey(t.getAggregateId())) {
                tournamentPerAggregateId.put(t.getAggregateId(), t);
            } else {
                if(tournamentPerAggregateId.get(t.getAggregateId()).getCreationTs().isBefore(t.getCreationTs())) {
                    tournamentPerAggregateId.put(t.getAggregateId(), t);
                }
            }
        }
        return (Set<Tournament>) tournamentPerAggregateId.values();
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TournamentDto updateTournament(TournamentDto tournamentDto, Set<TournamentTopic> tournamentTopics, UnitOfWork unitOfWorkWorkService) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentDto.getAggregateId(), unitOfWorkWorkService);
        Tournament newTournament = new Tournament(oldTournament);

        if(tournamentDto.getStartTime() != null ) {
            newTournament.setStartTime(LocalDateTime.
                    parse(tournamentDto.getStartTime()));
            unitOfWorkWorkService.addAggregateToCommit(newTournament);
        }

        if(tournamentDto.getEndTime() != null ) {
            newTournament.setEndTime(LocalDateTime.parse(tournamentDto.getEndTime()));
            unitOfWorkWorkService.addAggregateToCommit(newTournament);
        }

        if(tournamentDto.getNumberOfQuestions() != null ) {
            newTournament.setNumberOfQuestions(tournamentDto.getNumberOfQuestions());
            unitOfWorkWorkService.addAggregateToCommit(newTournament);
        }

        if(tournamentTopics != null && !tournamentTopics.isEmpty() ) {
            newTournament.setTopics(tournamentTopics);
            unitOfWorkWorkService.addAggregateToCommit(newTournament);
        }

        return new TournamentDto(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<TournamentDto> getTournamentsForCourseExecution(Integer executionAggregateId, UnitOfWork unitOfWorkWorkService) {
        /*switch this to query???*/
        return findAllTournamentByVersion(unitOfWorkWorkService).stream()
                .filter(t -> t.getCourseExecution().getAggregateId() == executionAggregateId)
                .map(TournamentDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<TournamentDto> getOpenedTournamentsForCourseExecution(Integer executionAggregateId, UnitOfWork unitOfWorkWorkService) {
        /*switch this to query???*/
        LocalDateTime now = LocalDateTime.now();
        return findAllTournamentByVersion(unitOfWorkWorkService).stream()
                .filter(t -> t.getCourseExecution().getAggregateId() == executionAggregateId)
                .filter(t -> now.isBefore(t.getEndTime()))
                .filter(t -> !t.isCancelled())
                .map(TournamentDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<TournamentDto> getClosedTournamentsForCourseExecution(Integer executionAggregateId, UnitOfWork unitOfWorkWorkService) {
        /*switch this to query???*/
        LocalDateTime now = LocalDateTime.now();
        return findAllTournamentByVersion(unitOfWorkWorkService).stream()
                .filter(t -> t.getCourseExecution().getAggregateId() == executionAggregateId)
                .filter(t -> now.isAfter(t.getEndTime()))
                .filter(t -> !t.isCancelled())
                .map(TournamentDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void leaveTournament(Integer tournamentAggregateId, Integer userAggregateId, UnitOfWork unitOfWorkWorkService) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWorkWorkService);
        Tournament newTournament = new Tournament(oldTournament);
        TournamentParticipant participantToRemove = newTournament.findParticipant(userAggregateId);
        if(participantToRemove == null) {
            throw new TutorException(TOURNAMENT_PARTICIPANT_NOT_FOUND, userAggregateId, tournamentAggregateId);
        }
        newTournament.removeParticipant(participantToRemove);
        unitOfWorkWorkService.addAggregateToCommit(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void solveQuiz(Integer tournamentAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        Tournament newTournament = new Tournament(oldTournament);
        TournamentParticipant participant = newTournament.findParticipant(userAggregateId);
        if(participant == null) {
            throw new TutorException(TOURNAMENT_PARTICIPANT_NOT_FOUND, userAggregateId, tournamentAggregateId);
        }
        participant.answerQuiz();
        unitOfWork.addAggregateToCommit(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void cancelTournament(Integer tournamentAggregateId, UnitOfWork unitOfWorkWorkService) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWorkWorkService);
        Tournament newTournament = new Tournament(oldTournament);
        newTournament.cancel();
        unitOfWorkWorkService.addAggregateToCommit(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeTournament(Integer tournamentAggregateId, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        Tournament newTournament = new Tournament(oldTournament);
        newTournament.remove();
        unitOfWork.addAggregateToCommit(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeCourseExecution(Integer tournamentAggregateId, Integer courseExecutionId, Integer eventVersion, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        if(oldTournament.getCourseExecution() != null && oldTournament.getCourseExecution().getVersion() >= eventVersion) {
            return;
        }

        Tournament newTournament = new Tournament(oldTournament);
        if(newTournament.getCourseExecution().getAggregateId().equals(courseExecutionId)) {
            newTournament.remove();
            unitOfWork.addAggregateToCommit(newTournament);
        }

    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeUser(Integer tournamentAggregateId, Integer userAggregateId, Integer eventVersion, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        if(oldTournament.getCreator().getAggregateId().equals(userAggregateId) && oldTournament.getCreator().getVersion() >= eventVersion) {
            return;
        }

        TournamentParticipant oldParticipant = oldTournament.findParticipant(userAggregateId);
        if(oldParticipant != null && oldParticipant.getAggregateId().equals(userAggregateId) && oldParticipant.getVersion() >= tournamentAggregateId) {
            return;
        }
        Tournament newTournament = new Tournament(oldTournament);
        if(newTournament.getCreator().getAggregateId().equals(userAggregateId)) {
            newTournament.setState(INACTIVE);
            unitOfWork.addAggregateToCommit(newTournament);
        }
        TournamentParticipant tournamentParticipant  = newTournament.findParticipant(userAggregateId);
        if(tournamentParticipant == null) {
            throw new TutorException(TOURNAMENT_TOPIC_NOT_FOUND, userAggregateId, tournamentAggregateId);
        }
        tournamentParticipant.setState(DELETED);
        tournamentParticipant.setVersion(eventVersion);
        unitOfWork.addAggregateToCommit(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateTopic(Integer tournamentAggregateId, Integer topicAggregateId, String topicName, Integer eventVersion, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);

        TournamentTopic oldTopic = oldTournament.findTopic(topicAggregateId);
        if(oldTopic != null && oldTopic.getVersion() >= eventVersion) {
            return;
        }

        Tournament newTournament = new Tournament(oldTournament);
        TournamentTopic topic = newTournament.findTopic(topicAggregateId);
        if(topic == null) {
            throw new TutorException(TOURNAMENT_TOPIC_NOT_FOUND, topicAggregateId, tournamentAggregateId);
        }
        topic.setName(topicName);
        topic.setVersion(eventVersion);
        unitOfWork.addAggregateToCommit(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateParticipantAnswer(Integer tournamentAggregateId, Integer userAggregateId, Integer answerAggregateId, boolean isCorrect, Integer eventVersion, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        TournamentParticipant oldParticipant = oldTournament.findParticipant(userAggregateId);
        if(oldParticipant != null && oldParticipant.getAnswer().getVersion() >= eventVersion) {
            return;
        }
        Tournament newTournament = new Tournament(oldTournament);
        TournamentParticipant tournamentParticipant = newTournament.findParticipant(userAggregateId);
        if(tournamentParticipant == null) {
            throw new TutorException(TOURNAMENT_PARTICIPANT_NOT_FOUND, userAggregateId, tournamentAggregateId);
        }
        tournamentParticipant.updateAnswerWithQuestion(answerAggregateId, isCorrect, eventVersion);
        unitOfWork.addAggregateToCommit(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeTopic(Integer tournamentAggregateId, Integer topicAggregateId, Integer eventVersion, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        TournamentTopic oldTopic = oldTournament.findTopic(topicAggregateId);
        if(oldTopic != null && oldTopic.getVersion() >= eventVersion) {
            return;
        }
        Tournament newTournament = new Tournament(oldTournament);
        TournamentTopic tournamentTopic  = newTournament.findTopic(topicAggregateId);
        if(tournamentTopic == null) {
            throw new TutorException(TOURNAMENT_TOPIC_NOT_FOUND, topicAggregateId, tournamentAggregateId);
        }
        tournamentTopic.setState(INACTIVE);
        tournamentTopic.setVersion(eventVersion);
        QuizDto quizDto = new QuizDto();
        quizDto.setAggregateId(newTournament.getQuiz().getAggregateId());
        quizDto.setAvailableDate(newTournament.getStartTime().toString());
        quizDto.setConclusionDate(newTournament.getEndTime().toString());
        quizDto.setResultsDate(newTournament.getEndTime().toString());
        quizService.updateGeneratedQuiz(quizDto, newTournament.getTopics().stream().filter(t -> t.getState() == ACTIVE).map(TournamentTopic::getAggregateId).collect(Collectors.toSet()), newTournament.getNumberOfQuestions(),unitOfWork);
        unitOfWork.addAggregateToCommit(newTournament);
    }
}
