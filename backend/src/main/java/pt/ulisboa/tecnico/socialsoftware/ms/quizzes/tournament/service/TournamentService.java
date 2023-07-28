package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.repository.CausalConsistencyRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.service.CausalConsistencyService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.tcc.TournamentTCC;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.tcc.TournamentTCCRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.domain.TournamentParticipant;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.domain.TournamentTopic;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.service.QuizService;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.utils.DateHandler;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.ErrorMessage.*;

@Service
public class TournamentService {
    @Autowired
    private CausalConsistencyService causalConsistencyService;

    @Autowired
    private CausalConsistencyRepository causalConsistencyRepository;

    @Autowired
    private TournamentTCCRepository tournamentTCCRepository;

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
    public TournamentDto addTournamentCausalSnapshot(Integer aggregateId, UnitOfWork unitOfWork) {
        return new TournamentDto((TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(aggregateId, unitOfWork));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TournamentDto createTournament(TournamentDto tournamentDto, UserDto creatorDto,
                                          CourseExecutionDto courseExecutionDto, Set<TopicDto> topicDtos,
                                          QuizDto quizDto, UnitOfWork unitOfWork) {

        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();

        TournamentTCC tournament = new TournamentTCC(aggregateId, tournamentDto, creatorDto, courseExecutionDto, topicDtos, quizDto); /* should the skeleton creation be part of the functionality?? */

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

        TournamentTCC oldTournament = (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(tournamentAggregateId, unitOfWork);

        if (DateHandler.now().isAfter(oldTournament.getStartTime())) {
            throw new TutorException(CANNOT_ADD_PARTICIPANT, tournamentAggregateId);
        }

        // verification not needed anymore because request is made to course execution aggregate in which all are students
        /*if(!userRole.equals(STUDENT.toString())) {
            throw new TutorException(PARTICIPANT_NOT_STUDENT, tournamentParticipant.getAggregateId(), tournamentAggregateId);
        }*/
        TournamentTCC newTournament = new TournamentTCC(oldTournament);

        newTournament.addParticipant(tournamentParticipant);

        unitOfWork.registerChanged(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TournamentDto updateTournament(TournamentDto tournamentDto, Set<TopicDto> topicDtos, UnitOfWork unitOfWork) {
        TournamentTCC oldTournament = (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(tournamentDto.getAggregateId(), unitOfWork);
        TournamentTCC newTournament = new TournamentTCC(oldTournament);

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

        if (topicDtos != null && !topicDtos.isEmpty() ) {
            Set<TournamentTopic> tournamentTopics = topicDtos.stream()
                    .map(TournamentTopic::new)
                    .collect(Collectors.toSet());

            newTournament.setTournamentTopics(tournamentTopics);
            unitOfWork.registerChanged(newTournament);
        }

        return new TournamentDto(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<TournamentDto> getTournamentsForCourseExecution(Integer executionAggregateId, UnitOfWork unitOfWork) {
        return tournamentTCCRepository.findAllTournamentIdsOfNotDeletedAndNotInactiveByCourseExecution(executionAggregateId).stream()
                .map(aggregateId -> (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(aggregateId, unitOfWork))
                .map(TournamentDto::new)
                .collect(Collectors.toList());

    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<TournamentDto> getOpenedTournamentsForCourseExecution(Integer executionAggregateId, UnitOfWork unitOfWork) {
        LocalDateTime now = LocalDateTime.now();
        return tournamentTCCRepository.findAllTournamentIdsOfNotDeletedAndNotInactiveByCourseExecution(executionAggregateId).stream()
                .map(aggregateId -> (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(aggregateId, unitOfWork))
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
        return tournamentTCCRepository.findAllTournamentIdsOfNotDeletedAndNotInactiveByCourseExecution(executionAggregateId).stream()
                .map(aggregateId -> (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(aggregateId, unitOfWork))
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
        TournamentTCC oldTournament = (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(tournamentAggregateId, unitOfWork);
        TournamentTCC newTournament = new TournamentTCC(oldTournament);
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
        TournamentTCC oldTournament = (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(tournamentAggregateId, unitOfWork);
        TournamentTCC newTournament = new TournamentTCC(oldTournament);
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
        TournamentTCC oldTournament = (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(tournamentAggregateId, unitOfWork);
        TournamentTCC newTournament = new TournamentTCC(oldTournament);
        newTournament.cancel();
        unitOfWork.registerChanged(newTournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeTournament(Integer tournamentAggregateId, UnitOfWork unitOfWork) {
        TournamentTCC oldTournament = (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(tournamentAggregateId, unitOfWork);
        TournamentTCC newTournament = new TournamentTCC(oldTournament);
        newTournament.remove();
        unitOfWork.registerChanged(newTournament);
    }


    /******************************************* EVENT PROCESSING SERVICES ********************************************/

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Tournament anonymizeUser(Integer tournamentAggregateId, Integer executionAggregateId, Integer userAggregateId, String name, String username, Integer eventVersion, UnitOfWork unitOfWork) {
        TournamentTCC oldTournament = (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(tournamentAggregateId, unitOfWork);
        TournamentTCC newTournament = new TournamentTCC(oldTournament);

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
        TournamentTCC oldTournament = (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(tournamentAggregateId, unitOfWork);
        if (oldTournament.getTournamentCourseExecution() != null && oldTournament.getTournamentCourseExecution().getCourseExecutionVersion() >= eventVersion) {
            return null;
        }

        TournamentTCC newTournament = new TournamentTCC(oldTournament);
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
        TournamentTCC oldTournament = (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(tournamentAggregateId, unitOfWork);


        TournamentTCC newTournament = new TournamentTCC(oldTournament);
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
        TournamentTCC oldTournament = (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(tournamentAggregateId, unitOfWork);
        TournamentTCC newTournament = new TournamentTCC(oldTournament);
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
        TournamentTCC oldTournament = (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(tournamentAggregateId, unitOfWork);
        TournamentTopic oldTopic = oldTournament.findTopic(topicAggregateId);
        if (oldTopic != null && oldTopic.getTopicVersion() >= eventVersion) {
            return null;
        }
        TournamentTCC newTournament = new TournamentTCC(oldTournament);
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
            quizService.updateGeneratedQuiz(quizDto, newTournament.getTournamentTopics().stream().filter(t -> t.getState() == Aggregate.AggregateState.ACTIVE).map(TournamentTopic::getTopicAggregateId).collect(Collectors.toSet()), newTournament.getNumberOfQuestions(), unitOfWork);
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
        TournamentTCC oldTournament = (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(tournamentAggregateId, unitOfWork);
        TournamentParticipant oldParticipant = oldTournament.findParticipant(studentAggregateId);
        if (oldParticipant != null && oldParticipant.getParticipantAnswer().getQuizAnswerVersion() >= eventVersion) {
            return null;
        }
        TournamentTCC newTournament = new TournamentTCC(oldTournament);
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
        TournamentTCC oldTournament = (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(tournamentAggregateId, unitOfWork);
        TournamentTCC newTournament = new TournamentTCC(oldTournament);
        List<Integer> topicsIds = newTournament.getTournamentTopics().stream().map(TournamentTopic::getTopicAggregateId).collect(Collectors.toList());

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
        TournamentTCC oldTournament = (TournamentTCC) causalConsistencyService.addAggregateCausalSnapshot(tournamentAggregateId, unitOfWork);
        TournamentTCC newTournament = new TournamentTCC(oldTournament);

        if (!newTournament.getTournamentCourseExecution().getCourseExecutionAggregateId().equals(executionAggregateId)) {
            return;
        }

        if (newTournament.getTournamentCreator().getCreatorAggregateId().equals(userAggregateId)) {
            newTournament.getTournamentCreator().setCreatorName(name);
            newTournament.getTournamentCourseExecution().setCourseExecutionVersion(eventVersion);
            unitOfWork.registerChanged(newTournament);
        }

        for (TournamentParticipant tp : newTournament.getTournamentParticipants()) {
            if (tp.getParticipantAggregateId().equals(userAggregateId)) {
                tp.setParticipantName(name);
                newTournament.getTournamentCourseExecution().setCourseExecutionVersion(eventVersion);
                unitOfWork.registerChanged(newTournament);
            }
        }
    }
}