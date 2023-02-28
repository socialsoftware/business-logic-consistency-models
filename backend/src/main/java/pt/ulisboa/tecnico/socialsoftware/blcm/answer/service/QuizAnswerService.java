package pt.ulisboa.tecnico.socialsoftware.blcm.answer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto.QuestionAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto.QuizAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.event.publish.QuizAnswerQuestionAnswerEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.repository.QuizAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class QuizAnswerService {

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private QuizService quizService;

    @Autowired
    private CourseExecutionService courseExecutionService;

    @Autowired
    private EventRepository eventRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizAnswerDto getCausalQuizAnswerRemove(Integer aggregateId, UnitOfWork unitOfWork) {
        return new QuizAnswerDto(getCausalQuizAnswerLocal(aggregateId, unitOfWork));
    }

    public QuizAnswer getCausalQuizAnswerLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        QuizAnswer quizAnswer = quizAnswerRepository.findCausal(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(ErrorMessage.QUIZ_ANSWER_NOT_FOUND, aggregateId));

        if(quizAnswer.getState() == Aggregate.AggregateState.DELETED) {
            throw new TutorException(ErrorMessage.QUIZ_ANSWER_DELETED, quizAnswer.getAggregateId());
        }

        List<Event> allEvents = eventRepository.findAll();
        unitOfWork.addToCausalSnapshot(quizAnswer, allEvents);
        return quizAnswer;
    }

    public QuizAnswer getCausalQuizAnswerLocalByQuizAndUser(Integer quizAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        QuizAnswer quizAnswer = quizAnswerRepository.findCausalByQuizAndUser(quizAggregateId, userAggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(ErrorMessage.NO_USER_ANSWER_FOR_QUIZ, quizAggregateId, userAggregateId));

        if(quizAnswer.getState() == Aggregate.AggregateState.DELETED) {
            throw new TutorException(ErrorMessage.QUIZ_ANSWER_DELETED, quizAnswer.getAggregateId());
        }

        List<Event> allEvents = eventRepository.findAll();
        unitOfWork.addToCausalSnapshot(quizAnswer, allEvents);
        return quizAnswer;
    }



    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizAnswerDto startQuiz(Integer quizAggregateId, Integer courseExecutionAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        QuizDto quizDto = quizService.getCausalQuizRemote(quizAggregateId, unitOfWork);

        // COURSE_EXECUTION_SAME_QUIZ_COURSE_EXECUTION
        if(!courseExecutionAggregateId.equals(quizDto.getAggregateId())) {
            throw new TutorException(ErrorMessage.QUIZ_DOES_NOT_BELONG_TO_COURSE_EXECUTION, quizAggregateId, courseExecutionAggregateId);
        }

        // QUIZ_COURSE_EXECUTION_SAME_AS_USER_COURSE_EXECUTION
        // COURSE_EXECUTION_SAME_AS_USER_COURSE_EXECUTION
        UserDto userDto = courseExecutionService.getStudentByExecutionIdAndUserId(userAggregateId, quizDto.getCourseExecutionAggregateId(), unitOfWork);

        // QUESTIONS_ANSWER_QUESTIONS_BELONG_TO_QUIZ because questions come from the quiz
        QuizAnswer quizAnswer = new QuizAnswer(aggregateId, new AnswerCourseExecution(quizDto.getCourseExecutionAggregateId(), quizDto.getCourseExecutionVersion()), new AnswerStudent(userDto), new AnsweredQuiz(quizDto));
        quizAnswer.setAnswerDate(LocalDateTime.now());
        unitOfWork.registerChanged(quizAnswer);
        return new QuizAnswerDto(quizAnswer);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void answerQuestion(Integer quizAggregateId, Integer userAggregateId, QuestionAnswerDto userAnswerDto, QuestionDto questionDto, UnitOfWork unitOfWork) {
        QuizAnswer oldQuizAnswer = getCausalQuizAnswerLocalByQuizAndUser(quizAggregateId, userAggregateId, unitOfWork);
        QuizAnswer newQuizAnswer = new QuizAnswer(oldQuizAnswer);

        QuestionAnswer questionAnswer = new QuestionAnswer(userAnswerDto, questionDto);
        newQuizAnswer.addQuestionAnswer(questionAnswer);
        unitOfWork.registerChanged(newQuizAnswer);
        unitOfWork.addEvent(new QuizAnswerQuestionAnswerEvent(newQuizAnswer.getAggregateId(), questionAnswer.getQuestionAggregateId(), quizAggregateId, userAggregateId, questionAnswer.isCorrect()));
    }


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void concludeQuiz(Integer quizAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        QuizAnswer oldQuizAnswer = getCausalQuizAnswerLocalByQuizAndUser(quizAggregateId, userAggregateId, unitOfWork);
        QuizAnswer newQuizAnswer = new QuizAnswer(oldQuizAnswer);

        newQuizAnswer.setCompleted(true);
        unitOfWork.registerChanged(newQuizAnswer);
    }

    // EVENT DETECTION SUBSCRIPTIONS
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Set<EventSubscription> getEventSubscriptions(Integer aggregateId, Integer versionId, String eventType) {
        QuizAnswer quizAnswer = quizAnswerRepository.findVersionByAggregateIdAndVersionId(aggregateId, versionId).get();
        return quizAnswer.getEventSubscriptionsByEventType(eventType);
    }

    /************************************************ EVENT PROCESSING ************************************************/

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateUserName(Integer answerAggregateId, Integer executionAggregateId, Integer eventVersion, Integer userAggregateId, String name, UnitOfWork unitOfWork) {
        QuizAnswer oldQuizAnswer = getCausalQuizAnswerLocal(answerAggregateId, unitOfWork);
        QuizAnswer newQuizAnswer = new QuizAnswer(oldQuizAnswer);

        if (!newQuizAnswer.getCourseExecution().getCourseExecutionAggregateId().equals(executionAggregateId)) {
            return;
        }

        if (newQuizAnswer.getStudent().getStudentAggregateId().equals(userAggregateId)) {
            newQuizAnswer.getStudent().setName(name);
            newQuizAnswer.getCourseExecution().setCourseExecutionVersion(eventVersion);
            unitOfWork.registerChanged(newQuizAnswer);
        }
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizAnswer removeUser(Integer answerAggregateId, Integer userAggregateId, Integer aggregateVersion, UnitOfWork unitOfWork) {
        QuizAnswer oldQuizAnswer = getCausalQuizAnswerLocal(answerAggregateId, unitOfWork);
        if (oldQuizAnswer != null && oldQuizAnswer.getStudent().getStudentAggregateId().equals(userAggregateId) && oldQuizAnswer.getVersion() >= aggregateVersion) {
            return null;
        }

        QuizAnswer newQuizAnswer = new QuizAnswer(oldQuizAnswer);
        newQuizAnswer.getStudent().setStudentState(Aggregate.AggregateState.DELETED);
        newQuizAnswer.setState(Aggregate.AggregateState.INACTIVE);
        unitOfWork.registerChanged(newQuizAnswer);
        return newQuizAnswer;
    }

    public QuizAnswer removeQuestion(Integer answerAggregateId, Integer questionAggregateId, Integer aggregateVersion, UnitOfWork unitOfWork) {
        QuizAnswer oldQuizAnswer = getCausalQuizAnswerLocal(answerAggregateId, unitOfWork);
        QuestionAnswer questionAnswer = oldQuizAnswer.findQuestionAnswer(questionAggregateId);

        if (questionAnswer == null) {
            return null;
        }

        QuizAnswer newQuizAnswer = new QuizAnswer(oldQuizAnswer);
        questionAnswer.setState(Aggregate.AggregateState.DELETED);
        newQuizAnswer.setState(Aggregate.AggregateState.INACTIVE);
        unitOfWork.registerChanged(newQuizAnswer);
        return newQuizAnswer;
    }
}
