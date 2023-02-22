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
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.AnswerQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.repository.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.repository.AnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnswerService {

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private AnswerRepository answerRepository;

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

    public Answer getCausalQuizAnswerLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        Answer answer = answerRepository.findCausal(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(ErrorMessage.QUIZ_ANSWER_NOT_FOUND, aggregateId));

        if(answer.getState() == Aggregate.AggregateState.DELETED) {
            throw new TutorException(ErrorMessage.QUIZ_ANSWER_DELETED, answer.getAggregateId());
        }

        List<Event> allEvents = eventRepository.findAll();
        unitOfWork.addToCausalSnapshot(answer, allEvents);
        return answer;
    }

    public Answer getCausalQuizAnswerLocalByQuizAndUser(Integer quizAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        Answer answer = answerRepository.findCausalByQuizAndUser(quizAggregateId, userAggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(ErrorMessage.NO_USER_ANSWER_FOR_QUIZ, quizAggregateId, userAggregateId));

        if(answer.getState() == Aggregate.AggregateState.DELETED) {
            throw new TutorException(ErrorMessage.QUIZ_ANSWER_DELETED, answer.getAggregateId());
        }

        List<Event> allEvents = eventRepository.findAll();
        unitOfWork.addToCausalSnapshot(answer, allEvents);
        return answer;
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
        Answer answer = new Answer(aggregateId, new AnswerCourseExecution(quizDto.getCourseExecutionAggregateId(), quizDto.getCourseExecutionVersion()), new AnswerUser(userDto), new AnswerQuiz(quizDto));
        answer.setAnswerDate(LocalDateTime.now());
        unitOfWork.registerChanged(answer);
        return new QuizAnswerDto(answer);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void answerQuestion(Integer quizAggregateId, Integer userAggregateId, QuestionAnswerDto userAnswerDto, QuestionDto questionDto, UnitOfWork unitOfWork) {
        Answer oldAnswer = getCausalQuizAnswerLocalByQuizAndUser(quizAggregateId, userAggregateId, unitOfWork);
        Answer newAnswer = new Answer(oldAnswer);

        QuestionAnswer questionAnswer = new QuestionAnswer(userAnswerDto, questionDto);
        newAnswer.addQuestionAnswer(questionAnswer);
        unitOfWork.registerChanged(newAnswer);
        unitOfWork.addEvent(new AnswerQuestionEvent(questionAnswer, newAnswer, quizAggregateId));
    }


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void concludeQuiz(Integer quizAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        Answer oldAnswer = getCausalQuizAnswerLocalByQuizAndUser(quizAggregateId, userAggregateId, unitOfWork);
        Answer newAnswer = new Answer(oldAnswer);

        newAnswer.setCompleted(true);
        unitOfWork.registerChanged(newAnswer);
    }

    /************************************************ EVENT PROCESSING ************************************************/
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Answer removeUser(Integer answerAggregateId, Integer userAggregateId, Integer aggregateVersion, UnitOfWork unitOfWork) {
        Answer oldAnswer = getCausalQuizAnswerLocal(answerAggregateId, unitOfWork);
        if(oldAnswer != null && oldAnswer.getUser().getUserAggregateId().equals(userAggregateId) && oldAnswer.getVersion() >= aggregateVersion) {
            return null;
        }

        Answer newAnswer = new Answer(oldAnswer);
        newAnswer.getUser().setUserState(Aggregate.AggregateState.DELETED);
        newAnswer.setState(Aggregate.AggregateState.INACTIVE);
        unitOfWork.registerChanged(newAnswer);
        return newAnswer;
    }

    public Answer removeQuestion(Integer answerAggregateId, Integer questionAggregateId, Integer aggregateVersion, UnitOfWork unitOfWork) {
        Answer oldAnswer = getCausalQuizAnswerLocal(answerAggregateId, unitOfWork);
        QuestionAnswer questionAnswer = oldAnswer.findQuestionAnswer(questionAggregateId);

        if(questionAnswer == null) {
            return null;
        }

        Answer newAnswer = new Answer(oldAnswer);
        questionAnswer.setState(Aggregate.AggregateState.DELETED);
        newAnswer.setState(Aggregate.AggregateState.INACTIVE);
        unitOfWork.registerChanged(newAnswer);
        return newAnswer;
    }
}
