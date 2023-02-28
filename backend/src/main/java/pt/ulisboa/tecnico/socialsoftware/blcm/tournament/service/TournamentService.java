package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private EventRepository eventRepository;

    // intended for requests from external functionalities
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TournamentDto getCausalTournamentRemote(Integer aggregateId, UnitOfWork unitOfWork) {
        return new TournamentDto(getCausalTournamentLocal(aggregateId, unitOfWork));
    }

    // intended for requests from local functionalities

    private Tournament getCausalTournamentLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        Tournament tournament = tournamentRepository.findCausal(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, aggregateId));

        if (tournament.getState() == Aggregate.AggregateState.DELETED) {
            throw new TutorException(TOURNAMENT_DELETED, tournament.getAggregateId());
        }

        List<Event> allEvents = eventRepository.findAll();

        unitOfWork.addToCausalSnapshot(tournament, allEvents);
        return tournament;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TournamentDto createTournament(TournamentDto tournamentDto, TournamentCreator creator,
                                          TournamentCourseExecution courseExecution, Set<TournamentTopic> topics,
                                          TournamentQuiz quiz, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        if (creator.getCreatorName().equals("ANONYMOUS") || creator.getCreatorUsername().equals("ANONYMOUS")) {
            throw new TutorException(ErrorMessage.USER_IS_ANONYMOUS, creator.getCreatorAggregateId());
        }
        Tournament tournament = new Tournament(aggregateId, tournamentDto, creator, courseExecution, topics, quiz); /* should the skeleton creation be part of the functionality?? */

        unitOfWork.registerChanged(tournament);
        return new TournamentDto(tournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void addParticipant(Integer tournamentAggregateId, TournamentParticipant tournamentParticipant, String userRole, UnitOfWork unitOfWork) {
        if (tournamentParticipant.getParticipantName().equals("ANONYMOUS") || tournamentParticipant.getParticipantUsername().equals("ANONYMOUS")) {
            throw new TutorException(ErrorMessage.USER_IS_ANONYMOUS, tournamentParticipant.getParticipantAggregateId());
        }
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        if (DateHandler.now().isAfter(oldTournament.getStartTime())) {
            throw new TutorException(CANNOT_ADD_PARTICIPANT, tournamentAggregateId);
        }

        // verification not needed anymore because request is made to course execution aggregate in which all are students
        /*if(!userRole.equals(STUDENT.toString())) {
            throw new TutorException(PARTICIPANT_NOT_STUDENT, tournamentParticipant.getAggregateId(), tournamentAggregateId);
        }*/
        Tournament newTournament = new Tournament(oldTournament);

        newTournament.addParticipant(tournamentParticipant);

        unitOfWork.registerChanged(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TournamentDto updateTournament(TournamentDto tournamentDto, Set<TournamentTopic> tournamentTopics, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentDto.getAggregateId(), unitOfWork);
        Tournament newTournament = new Tournament(oldTournament);

        if (tournamentDto.getStartTime() != null ) {
            newTournament.setStartTime(DateHandler.toLocalDateTime(tournamentDto.getStartTime()));
            unitOfWork.registerChanged(newTournament);
        }

        if (tournamentDto.getEndTime() != null ) {
            newTournament.setEndTime(DateHandler.toLocalDateTime(tournamentDto.getEndTime()));
            unitOfWork.registerChanged(newTournament);
        }

        if (tournamentDto.getNumberOfQuestions() != null ) {
            newTournament.setNumberOfQuestions(tournamentDto.getNumberOfQuestions());
            unitOfWork.registerChanged(newTournament);
        }

        if (tournamentTopics != null && !tournamentTopics.isEmpty() ) {
            newTournament.setTopics(tournamentTopics);
            unitOfWork.registerChanged(newTournament);
        }

        return new TournamentDto(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<TournamentDto> getTournamentsForCourseExecution(Integer executionAggregateId, UnitOfWork unitOfWork) {
        return tournamentRepository.findAllAggregateIdsOfNotDeletedAndNotInactiveByCourseExecution(executionAggregateId).stream()
                .map(aggregateId -> getCausalTournamentLocal(aggregateId, unitOfWork))
                .map(TournamentDto::new)
                .collect(Collectors.toList());

    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<TournamentDto> getOpenedTournamentsForCourseExecution(Integer executionAggregateId, UnitOfWork unitOfWork) {
        LocalDateTime now = LocalDateTime.now();
        return tournamentRepository.findAllAggregateIdsOfNotDeletedAndNotInactiveByCourseExecution(executionAggregateId).stream()
                .map(aggregateId -> getCausalTournamentLocal(aggregateId, unitOfWork))
                .filter(t -> now.isBefore(t.getEndTime()))
                .filter(t -> !t.isCancelled())
                .map(TournamentDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<TournamentDto> getClosedTournamentsForCourseExecution(Integer executionAggregateId, UnitOfWork unitOfWork) {
        LocalDateTime now = LocalDateTime.now();
        return tournamentRepository.findAllAggregateIdsOfNotDeletedAndNotInactiveByCourseExecution(executionAggregateId).stream()
                .map(aggregateId -> getCausalTournamentLocal(aggregateId, unitOfWork))
                .filter(t -> now.isAfter(t.getEndTime()))
                .filter(t -> !t.isCancelled())
                .map(TournamentDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void leaveTournament(Integer tournamentAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        Tournament newTournament = new Tournament(oldTournament);
        TournamentParticipant participantToRemove = newTournament.findParticipant(userAggregateId);
        if (participantToRemove == null) {
            throw new TutorException(TOURNAMENT_PARTICIPANT_NOT_FOUND, userAggregateId, tournamentAggregateId);
        }
        newTournament.removeParticipant(participantToRemove);

        unitOfWork.registerChanged(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void solveQuiz(Integer tournamentAggregateId, Integer userAggregateId, Integer answerAggregateId, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        Tournament newTournament = new Tournament(oldTournament);
        TournamentParticipant participant = newTournament.findParticipant(userAggregateId);
        if (participant == null) {
            throw new TutorException(TOURNAMENT_PARTICIPANT_NOT_FOUND, userAggregateId, tournamentAggregateId);
        }
        participant.answerQuiz();
        unitOfWork.registerChanged(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void cancelTournament(Integer tournamentAggregateId, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        Tournament newTournament = new Tournament(oldTournament);
        newTournament.cancel();
        unitOfWork.registerChanged(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeTournament(Integer tournamentAggregateId, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        Tournament newTournament = new Tournament(oldTournament);
        newTournament.remove();
        unitOfWork.registerChanged(newTournament);
    }


    // EVENT DETECTION SUBSCRIPTIONS
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Set<EventSubscription> getEventSubscriptions(Integer aggregateId, Integer versionId, String eventType) {
        Tournament tournament = tournamentRepository.findVersionByAggregateIdAndVersionId(aggregateId, versionId).get();
        return tournament.getEventSubscriptionsByEventType(eventType);
    }

    /******************************************* EVENT PROCESSING SERVICES ********************************************/

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Tournament anonymizeUser(Integer tournamentAggregateId, Integer executionAggregateId, Integer userAggregateId, String name, String username, Integer eventVersion, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        Tournament newTournament = new Tournament(oldTournament);

        if (!newTournament.getTournamentCourseExecution().getCourseExecutionAggregateId().equals(executionAggregateId)) {
            return null;
        }

        if (newTournament.getTournamentCreator().getCreatorAggregateId().equals(userAggregateId)) {
            newTournament.getTournamentCreator().setCreatorName(name);
            newTournament.getTournamentCreator().setCreatorUsername(username);
            newTournament.getTournamentCourseExecution().setCourseExecutionVersion(eventVersion);
            newTournament.setState(Aggregate.AggregateState.INACTIVE);
            unitOfWork.registerChanged(newTournament);
        }

        for (TournamentParticipant tp : newTournament.getTournamentParticipants()) {
            if (tp.getParticipantAggregateId().equals(userAggregateId)) {
                tp.setParticipantName(name);
                tp.setParticipantUsername(username);
                newTournament.getTournamentCourseExecution().setCourseExecutionVersion(eventVersion);
                unitOfWork.registerChanged(newTournament);
            }
        }

        return newTournament;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Tournament removeCourseExecution(Integer tournamentAggregateId, Integer courseExecutionId, Integer eventVersion, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        if (oldTournament.getTournamentCourseExecution() != null && oldTournament.getTournamentCourseExecution().getCourseExecutionVersion() >= eventVersion) {
            return null;
        }

        Tournament newTournament = new Tournament(oldTournament);
        if (newTournament.getTournamentCourseExecution().getCourseExecutionAggregateId().equals(courseExecutionId)) {
            newTournament.setState(Aggregate.AggregateState.INACTIVE);
            unitOfWork.registerChanged(newTournament);
        }
        return newTournament;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Tournament removeUser(Integer tournamentAggregateId, Integer courseExecutionAggregateId, Integer userAggregateId, Integer eventVersion, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);


        Tournament newTournament = new Tournament(oldTournament);
        if (newTournament.getTournamentCreator().getCreatorAggregateId().equals(userAggregateId)) {
            newTournament.getTournamentCreator().setCreatorState(Aggregate.AggregateState.INACTIVE);
            newTournament.setState(Aggregate.AggregateState.INACTIVE);
            newTournament.getTournamentCourseExecution().setCourseExecutionVersion(eventVersion);
            unitOfWork.registerChanged(newTournament);
        }

        TournamentParticipant tournamentParticipant  = newTournament.findParticipant(userAggregateId);
        if (tournamentParticipant != null) {
            tournamentParticipant.setState(Aggregate.AggregateState.DELETED);
            newTournament.getTournamentCourseExecution().setCourseExecutionVersion(eventVersion);
            //tournamentParticipant.setVersion(eventVersion);
            unitOfWork.registerChanged(newTournament);
        }


        return newTournament;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Tournament updateTopic(Integer tournamentAggregateId, Integer topicAggregateId, String topicName, Integer eventVersion, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        Tournament newTournament = new Tournament(oldTournament);
        TournamentTopic topic = newTournament.findTopic(topicAggregateId);
        if (topic == null) {
            throw new TutorException(TOURNAMENT_TOPIC_NOT_FOUND, topicAggregateId, tournamentAggregateId);
        }
        topic.setTopicName(topicName);
        topic.setTopicVersion(eventVersion);
        unitOfWork.registerChanged(newTournament);

        return newTournament;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Tournament removeTopic(Integer tournamentAggregateId, Integer topicAggregateId, Integer eventVersion, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        TournamentTopic oldTopic = oldTournament.findTopic(topicAggregateId);
        if (oldTopic != null && oldTopic.getTopicVersion() >= eventVersion) {
            return null;
        }
        Tournament newTournament = new Tournament(oldTournament);
        TournamentTopic tournamentTopic  = newTournament.findTopic(topicAggregateId);
        if (tournamentTopic == null) {
            throw new TutorException(TOURNAMENT_TOPIC_NOT_FOUND, topicAggregateId, tournamentAggregateId);
        }
        newTournament.removeTopic(tournamentTopic);
        QuizDto quizDto = new QuizDto();
        quizDto.setAggregateId(newTournament.getTournamentQuiz().getQuizAggregateId());
        quizDto.setAvailableDate(newTournament.getStartTime().toString());
        quizDto.setConclusionDate(newTournament.getEndTime().toString());
        quizDto.setResultsDate(newTournament.getEndTime().toString());
        try {
            quizService.updateGeneratedQuiz(quizDto, newTournament.getTopics().stream().filter(t -> t.getState() == Aggregate.AggregateState.ACTIVE).map(TournamentTopic::getTopicAggregateId).collect(Collectors.toSet()), newTournament.getNumberOfQuestions(), unitOfWork);
        } catch (TutorException e) {
            newTournament.setState(Aggregate.AggregateState.INACTIVE);
        }

        unitOfWork.registerChanged(newTournament);
        return newTournament;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Tournament updateParticipantAnswer(Integer tournamentAggregateId, Integer studentAggregateId, Integer quizAnswerAggregateId, Integer questionAggregateId, boolean isCorrect, Integer eventVersion, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        TournamentParticipant oldParticipant = oldTournament.findParticipant(studentAggregateId);
        if (oldParticipant != null && oldParticipant.getParticipantAnswer().getQuizAnswerVersion() >= eventVersion) {
            return null;
        }
        Tournament newTournament = new Tournament(oldTournament);
        TournamentParticipant tournamentParticipant = newTournament.findParticipant(studentAggregateId);
        if (tournamentParticipant == null) {
            throw new TutorException(TOURNAMENT_PARTICIPANT_NOT_FOUND, studentAggregateId, tournamentAggregateId);
        }
        /*
            AFTER_END
                now > this.endTime => p: this.participant | final p.answer
            IS_CANCELED
                this.canceled => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final this.participants && p: this.participant | final p.answer
        */
        if (oldTournament != null) {
            if ((oldTournament.getStartTime() != null && LocalDateTime.now().isAfter(oldTournament.getStartTime())) || oldTournament.isCancelled()) {
                throw new TutorException(CANNOT_UPDATE_TOURNAMENT, oldTournament.getAggregateId());
            }
        }
        tournamentParticipant.updateAnswerWithQuestion(quizAnswerAggregateId, quizAnswerAggregateId, isCorrect, eventVersion);
        unitOfWork.registerChanged(newTournament);
        return newTournament;
    }



    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Tournament invalidateQuiz(Integer tournamentAggregateId, Integer aggregateId, Integer aggregateVersion, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        Tournament newTournament = new Tournament(oldTournament);
        List<Integer> topicsIds = newTournament.getTopics().stream().map(TournamentTopic::getTopicAggregateId).collect(Collectors.toList());

        QuizDto quizDto = new QuizDto();
        quizDto.setAvailableDate(newTournament.getStartTime().toString());
        quizDto.setConclusionDate(newTournament.getEndTime().toString());
        quizDto.setResultsDate(newTournament.getEndTime().toString());

        QuizDto quizDto1 = null;
        try {
            quizDto1 = quizService.generateQuiz(newTournament.getTournamentCourseExecution().getCourseExecutionAggregateId(), quizDto, topicsIds, newTournament.getNumberOfQuestions(), unitOfWork);
        } catch (TutorException e) {
            newTournament.setState(Aggregate.AggregateState.INACTIVE);
        }

        if (quizDto1 != null) {
            newTournament.getTournamentQuiz().setQuizAggregateId(quizDto1.getAggregateId());
            newTournament.getTournamentQuiz().setQuizVersion(quizDto1.getVersion());
            unitOfWork.registerChanged(newTournament);
        }

        return newTournament;

    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateUserName(Integer tournamentAggregateId, Integer executionAggregateId, Integer eventVersion, Integer userAggregateId, String name, UnitOfWork unitOfWork) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWork);
        Tournament newTournament = new Tournament(oldTournament);

        if (!newTournament.getTournamentCourseExecution().getCourseExecutionAggregateId().equals(executionAggregateId)) {
            return;
        }

        if (newTournament.getTournamentCreator().getCreatorAggregateId().equals(userAggregateId)) {
            newTournament.getTournamentCreator().setCreatorName(name);
            newTournament.getTournamentCourseExecution().setCourseExecutionVersion(eventVersion);
            unitOfWork.registerChanged(newTournament);
        }

        for (TournamentParticipant tp : newTournament.getTournamentParticipants()) {
            if(tp.getParticipantAggregateId().equals(userAggregateId)) {
                tp.setParticipantName(name);
                newTournament.getTournamentCourseExecution().setCourseExecutionVersion(eventVersion);
                unitOfWork.registerChanged(newTournament);
            }
        }
    }
}
