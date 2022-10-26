package pt.ulisboa.tecnico.socialsoftware.blcm.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto.QuizAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.service.AnswerService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.service.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.service.TopicService;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service.TournamentService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.service.UserService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.service.VersionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.ACTIVE;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

@Service
public class TournamentFunctionalities {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseExecutionService courseExecutionService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public TournamentDto createTournament(Integer userId, Integer executionId, List<Integer> topicsId,
                                          TournamentDto tournamentDto) {
        //unit of work code
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();

        checkInput(userId, topicsId, tournamentDto);

        // by making this call the invariants regarding the course execution and the role of the creator are guaranteed
        UserDto userDto = courseExecutionService.getStudentByExecutionIdAndUserId(executionId, userId, unitOfWork);
        TournamentCreator creator = new TournamentCreator(userDto.getAggregateId(), userDto.getName(), userDto.getUsername(), userDto.getVersion());

        CourseExecutionDto courseExecutionDto = courseExecutionService.getCausalCourseExecutionRemote(executionId, unitOfWork);
        TournamentCourseExecution tournamentCourseExecution = new TournamentCourseExecution(courseExecutionDto);


        Set<TournamentTopic> tournamentTopics = new HashSet<>();
        topicsId.forEach(topicId -> {
            TopicDto topicDto = topicService.getCausalTopicRemote(topicId, unitOfWork);
            tournamentTopics.add(new TournamentTopic(topicDto));

        });

        QuizDto quizDto = new QuizDto();
        quizDto.setAvailableDate(tournamentDto.getStartTime());
        quizDto.setConclusionDate(tournamentDto.getEndTime());
        quizDto.setResultsDate(tournamentDto.getEndTime());



        /*
        NUMBER_OF_QUESTIONS
            this.numberOfQuestions == Quiz(tournamentQuiz.id).quizQuestions.size
            Quiz(this.tournamentQuiz.id) DEPENDS ON this.numberOfQuestions
        QUIZ_TOPICS
            Quiz(this.tournamentQuiz.id) DEPENDS ON this.topics // the topics of the quiz questions are related to the tournament topics
        START_TIME_AVAILABLE_DATE
            this.startTime == Quiz(tournamentQuiz.id).availableDate
        END_TIME_CONCLUSION_DATE
            this.endTime == Quiz(tournamentQuiz.id).conclusionDate
         */
        QuizDto quizDto1 = quizService.generateQuiz(executionId, quizDto, topicsId, tournamentDto.getNumberOfQuestions(), unitOfWork);

        TournamentDto tournamentDto2 = tournamentService.createTournament(tournamentDto, creator, tournamentCourseExecution, tournamentTopics, new TournamentQuiz(quizDto1.getAggregateId(), quizDto1.getVersion()), unitOfWork);


        unitOfWorkService.commit(unitOfWork);

        return tournamentDto2;
    }

    public void addParticipant(Integer tournamentAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        TournamentDto tournamentDto = tournamentService.getCausalTournamentRemote(tournamentAggregateId, unitOfWork);
        // by making this call the invariants regarding the course execution and the role of the participant are guaranteed
        UserDto userDto = courseExecutionService.getStudentByExecutionIdAndUserId(tournamentDto.getCourseExecution().getAggregateId(), userAggregateId, unitOfWork);
        TournamentParticipant participant = new TournamentParticipant(userDto);
        tournamentService.addParticipant(tournamentAggregateId, participant, userDto.getRole(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void updateTournament(TournamentDto tournamentDto, Set<Integer> topicsAggregateIds) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();

        //checkInput(topicsAggregateIds, tournamentDto);

        Set<TournamentTopic> tournamentTopics = new HashSet<>();
        topicsAggregateIds.forEach(topicAggregateId -> {
            TopicDto topicDto = topicService.getCausalTopicRemote(topicAggregateId, unitOfWork);
            tournamentTopics.add(new TournamentTopic(topicDto));
        });

        TournamentDto newTournamentDto = tournamentService.updateTournament(tournamentDto, tournamentTopics, unitOfWork);

        QuizDto quizDto = new QuizDto();
        quizDto.setAggregateId(newTournamentDto.getQuiz().getAggregateId());
        quizDto.setAvailableDate(newTournamentDto.getStartTime());
        quizDto.setConclusionDate(newTournamentDto.getEndTime());
        quizDto.setResultsDate(newTournamentDto.getEndTime());

        /*
        NUMBER_OF_QUESTIONS
		    this.numberOfQuestions == Quiz(tournamentQuiz.id).quizQuestions.size
		    Quiz(this.tournamentQuiz.id) DEPENDS ON this.numberOfQuestions
        QUIZ_TOPICS
            Quiz(this.tournamentQuiz.id) DEPENDS ON this.topics // the topics of the quiz questions are related to the tournament topics
        START_TIME_AVAILABLE_DATE
            this.startTime == Quiz(tournamentQuiz.id).availableDate
        END_TIME_CONCLUSION_DATE
            this.endTime == Quiz(tournamentQuiz.id).conclusionDate
         */

        /*this if is required for the case of updating a quiz and not altering neither the number of questions neither the topics */
        if(topicsAggregateIds != null || tournamentDto.getNumberOfQuestions() != null) {
            if(topicsAggregateIds == null) {
                quizService.updateGeneratedQuiz(quizDto, newTournamentDto.getTopics().stream().filter(t -> t.getState().equals(ACTIVE.toString())).map(TopicDto::getAggregateId).collect(Collectors.toSet()), newTournamentDto.getNumberOfQuestions(), unitOfWork);
            } else {
                quizService.updateGeneratedQuiz(quizDto, topicsAggregateIds, newTournamentDto.getNumberOfQuestions(), unitOfWork);
            }
        }
        //quizService.updateGeneratedQuiz(quizDto, topicsAggregateIds, newTournamentDto.getNumberOfQuestions(), unitOfWork);


        unitOfWorkService.commit(unitOfWork);
    }




    public List<TournamentDto> getTournamentsForCourseExecution(Integer executionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return tournamentService.getTournamentsForCourseExecution(executionAggregateId, unitOfWork);
    }

    public List<TournamentDto> getOpenedTournamentsForCourseExecution(Integer executionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return tournamentService.getOpenedTournamentsForCourseExecution(executionAggregateId, unitOfWork);
    }

    public List<TournamentDto> getClosedTournamentsForCourseExecution(Integer executionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return tournamentService.getClosedTournamentsForCourseExecution(executionAggregateId, unitOfWork);
    }

    public void leaveTournament(Integer tournamentAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        tournamentService.leaveTournament(tournamentAggregateId, userAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public QuizDto solveQuiz(Integer tournamentAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        TournamentDto tournamentDto = tournamentService.getCausalTournamentRemote(tournamentAggregateId, unitOfWork);
        QuizDto quizDto = quizService.startTournamentQuiz(userAggregateId, tournamentDto.getQuiz().getAggregateId(), unitOfWork);
        QuizAnswerDto quizAnswerDto = answerService.startQuiz(tournamentDto.getQuiz().getAggregateId(), userAggregateId, unitOfWork);
        tournamentService.solveQuiz(tournamentAggregateId, userAggregateId, quizAnswerDto.getAggregateId(), unitOfWork);

        unitOfWorkService.commit(unitOfWork);
        return quizDto;
    }

    public void cancelTournament(Integer tournamentAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        tournamentService.cancelTournament(tournamentAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void removeTournament(Integer tournamentAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        tournamentService.removeTournament(tournamentAggregateId, unitOfWork);

        unitOfWorkService.commit(unitOfWork);
    }

    public TournamentDto findTournament(Integer tournamentAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return tournamentService.getCausalTournamentRemote(tournamentAggregateId, unitOfWork);
    }

    /** FOR TESTING PURPOSES **/
    public void getTournamentAndUser(Integer tournamentAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        TournamentDto tournamentDto = tournamentService.getCausalTournamentRemote(tournamentAggregateId, unitOfWork);
        UserDto userDto = userService.getCausalUserRemote(userAggregateId, unitOfWork);
        return;
    }



    private void checkInput(Integer userId, List<Integer> topicsId, TournamentDto tournamentDto) {
        if (userId == null) {
            throw new TutorException(TOURNAMENT_MISSING_USER);
        }
        if (topicsId == null) {
            throw new TutorException(TOURNAMENT_MISSING_TOPICS);
        }
        if (tournamentDto.getStartTime() == null) {
            throw new TutorException(TOURNAMENT_MISSING_START_TIME);
        }
        if (tournamentDto.getEndTime() == null) {
            throw new TutorException(TOURNAMENT_MISSING_END_TIME);
        }
        if (tournamentDto.getNumberOfQuestions() == null) {
            throw new TutorException(TOURNAMENT_MISSING_NUMBER_OF_QUESTIONS);
        }
    }

    private void checkInput(Set<Integer> topicsId, TournamentDto tournamentDto) {
        if (topicsId == null) {
            throw new TutorException(TOURNAMENT_MISSING_TOPICS);
        }
        if (tournamentDto.getStartTime() == null) {
            throw new TutorException(TOURNAMENT_MISSING_START_TIME);
        }
        if (tournamentDto.getEndTime() == null) {
            throw new TutorException(TOURNAMENT_MISSING_END_TIME);
        }
        if (tournamentDto.getNumberOfQuestions() == null) {
            throw new TutorException(TOURNAMENT_MISSING_NUMBER_OF_QUESTIONS);
        }
    }

    /************************************************ EVENT PROCESSING ************************************************/

    public void processAnonymizeStudentEvent(Integer aggregateId, Event eventToProcess) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();

        // TODO is this actually necessary? Dont think so can't anonymize a user if it doesnt exist in the current tournament version. Will do in the following.
        /*Tournament tournament = tournamentService.getCausalTournamentLocal(aggregateId, unitOfWork);
        Optional<EventSubscription> eventSubscriptionOp = tournament.getEventSubscriptionsByAggregateIdAndType(eventToProcess.getAggregateId(), eventToProcess.getType());
        if(eventSubscriptionOp.isPresent()) {
            if(eventSubscriptionOp.get().getSenderLastVersion() <= eventToProcess.getAggregateVersion()) {
                return;
            }
        }*/

        System.out.printf("Processing anonymize a user for course execution %d event for tournament %d\n", eventToProcess.getAggregateId(), aggregateId);
        AnonymizeExecutionStudentEvent anonymizeEvent = (AnonymizeExecutionStudentEvent) eventToProcess;
        tournamentService.anonymizeUser(aggregateId, anonymizeEvent.getAggregateId(), anonymizeEvent.getUserAggregateId(), anonymizeEvent.getName(), anonymizeEvent.getUsername(), anonymizeEvent.getAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void processRemoveCourseExecution (Integer aggregateId, Event eventToProcess) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        System.out.printf("Processing remove execution %d event for tournament %d\n", eventToProcess.getAggregateId(), aggregateId);
        RemoveCourseExecutionEvent removeCourseExecutionEvent = (RemoveCourseExecutionEvent) eventToProcess;
        tournamentService.removeCourseExecution(aggregateId, removeCourseExecutionEvent.getAggregateId(), removeCourseExecutionEvent.getAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    /*public void processRemoveUser(Integer aggregateId, Event eventToProcess) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        System.out.printf("Processing remove user %d event for tournament %d\n", eventToProcess.getAggregateId(), aggregateId);
        RemoveUserEvent removeUserEvent = (RemoveUserEvent) eventToProcess;
        tournamentService.removeUser(aggregateId, removeUserEvent.getAggregateId(), removeUserEvent.getAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }*/

    public void processUpdateTopic (Integer aggregateId, Event eventToProcess){
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        System.out.printf("Processing update topic %d event for tournament %d\n", eventToProcess.getAggregateId(), aggregateId);
        UpdateTopicEvent updateTopicEvent = (UpdateTopicEvent) eventToProcess;
        tournamentService.updateTopic(aggregateId, updateTopicEvent.getAggregateId(), updateTopicEvent.getTopicName(), updateTopicEvent.getAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void processDeleteTopic (Integer aggregateId, Event eventToProcess){
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        System.out.printf("Processing delete topic %d event for tournament %d\n", eventToProcess.getAggregateId(), aggregateId);
        DeleteTopicEvent deleteTopicEvent = (DeleteTopicEvent) eventToProcess;
        tournamentService.removeTopic(aggregateId, deleteTopicEvent.getAggregateId(), deleteTopicEvent.getAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void processAnswerQuestion(Integer aggregateId, Event eventToProcess) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        System.out.printf("Processing answer question %d event for tournament %d\n", eventToProcess.getAggregateId(), aggregateId);
        AnswerQuestionEvent answerQuestionEvent = (AnswerQuestionEvent) eventToProcess;
        tournamentService.updateParticipantAnswer(aggregateId, answerQuestionEvent.getUserAggregateId(), answerQuestionEvent.getAggregateId(), answerQuestionEvent.isCorrect(), answerQuestionEvent.getAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void processUnenrollStudent(Integer aggregateId, Event eventToProcess) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        System.out.printf("Processing unenroll student %d event for tournament %d\n", eventToProcess.getAggregateId(), aggregateId);
        UnerollStudentFromCourseExecutionEvent unerollStudentFromCourseExecutionEvent = (UnerollStudentFromCourseExecutionEvent) eventToProcess;
        tournamentService.removeUser(aggregateId, unerollStudentFromCourseExecutionEvent.getAggregateId(), unerollStudentFromCourseExecutionEvent.getUserAggregateId() ,unerollStudentFromCourseExecutionEvent.getAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void processInvalidateQuizEvent(Integer aggregateId, Event eventToProcess) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        System.out.printf("Processing invalidate quiz %d event for tournament %d\n", eventToProcess.getAggregateId(), aggregateId);
        InvalidateQuizEvent invalidateQuizEvent = (InvalidateQuizEvent) eventToProcess;
        tournamentService.invalidateQuiz(aggregateId, invalidateQuizEvent.getAggregateId(), invalidateQuizEvent.getAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void processUpdateExecutionStudentNameEvent(Integer aggregateId, Event eventToProcess) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        System.out.printf("Processing update execution student name of execution %d event for tournament %d\n", eventToProcess.getAggregateId(), aggregateId);
        UpdateExecutionStudentNameEvent updateExecutionStudentNameEvent = (UpdateExecutionStudentNameEvent) eventToProcess;
        tournamentService.updateUserName(aggregateId, updateExecutionStudentNameEvent.getAggregateId(), updateExecutionStudentNameEvent.getAggregateVersion(), updateExecutionStudentNameEvent.getUserAggregateId(), updateExecutionStudentNameEvent.getName(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
}
