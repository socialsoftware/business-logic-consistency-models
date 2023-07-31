package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.domain.*;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.dto.QuestionAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.dto.QuizAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.event.publish.QuizAnswerQuestionAnswerEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.repository.QuizAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.tcc.QuizAnswerTCC;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.service.QuizService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.dto.UserDto;

import java.sql.SQLException;
import java.time.LocalDateTime;

@Service
public class QuizAnswerService {

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private QuizService quizService;

    @Autowired
    private CourseExecutionService courseExecutionService;

    public QuizAnswer getQuizAnswerByQuizIdAndUserId(Integer quizAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        Integer quizAnswerId = quizAnswerRepository.findQuizAnswerIdByQuizIdAndUserId(quizAggregateId, userAggregateId)
                .orElseThrow(() -> new TutorException(ErrorMessage.NO_USER_ANSWER_FOR_QUIZ, quizAggregateId, userAggregateId));

        QuizAnswer quizAnswer = (QuizAnswer) unitOfWorkService.aggregateLoadAndRegisterRead(quizAnswerId, unitOfWork);

        if (quizAnswer.getState() == Aggregate.AggregateState.DELETED) {
            throw new TutorException(ErrorMessage.QUIZ_ANSWER_DELETED, quizAnswer.getAggregateId());
        }

        return quizAnswer;
    }



    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizAnswerDto startQuiz(Integer quizAggregateId, Integer courseExecutionAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        QuizDto quizDto = quizService.getQuizById(quizAggregateId, unitOfWork);

        // COURSE_EXECUTION_SAME_QUIZ_COURSE_EXECUTION
        if (!courseExecutionAggregateId.equals(quizDto.getAggregateId())) {
            throw new TutorException(ErrorMessage.QUIZ_DOES_NOT_BELONG_TO_COURSE_EXECUTION, quizAggregateId, courseExecutionAggregateId);
        }

        // QUIZ_COURSE_EXECUTION_SAME_AS_USER_COURSE_EXECUTION
        // COURSE_EXECUTION_SAME_AS_USER_COURSE_EXECUTION
        UserDto userDto = courseExecutionService.getStudentByExecutionIdAndUserId(userAggregateId, quizDto.getCourseExecutionAggregateId(), unitOfWork);

        // QUESTIONS_ANSWER_QUESTIONS_BELONG_TO_QUIZ because questions come from the quiz
        QuizAnswer quizAnswer = new QuizAnswerTCC(aggregateId, new AnswerCourseExecution(quizDto.getCourseExecutionAggregateId(), quizDto.getCourseExecutionVersion()), new AnswerStudent(userDto), new AnsweredQuiz(quizDto));
        quizAnswer.setAnswerDate(LocalDateTime.now());
        unitOfWork.registerChanged(quizAnswer);
        return new QuizAnswerDto(quizAnswer);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void answerQuestion(Integer quizAggregateId, Integer userAggregateId, QuestionAnswerDto userAnswerDto, QuestionDto questionDto, UnitOfWork unitOfWork) {
        QuizAnswer oldQuizAnswer = getQuizAnswerByQuizIdAndUserId(quizAggregateId, userAggregateId, unitOfWork);
        QuizAnswer newQuizAnswer = new QuizAnswerTCC((QuizAnswerTCC) oldQuizAnswer);

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
        QuizAnswer oldQuizAnswer = getQuizAnswerByQuizIdAndUserId(quizAggregateId, userAggregateId, unitOfWork);
        QuizAnswer newQuizAnswer = new QuizAnswerTCC((QuizAnswerTCC) oldQuizAnswer);

        newQuizAnswer.setCompleted(true);
        unitOfWork.registerChanged(newQuizAnswer);
    }

    /************************************************ EVENT PROCESSING ************************************************/

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateUserName(Integer answerAggregateId, Integer executionAggregateId, Integer eventVersion, Integer userAggregateId, String name, UnitOfWork unitOfWork) {
        QuizAnswer oldQuizAnswer = (QuizAnswer) unitOfWorkService.aggregateLoadAndRegisterRead(answerAggregateId, unitOfWork);
        QuizAnswer newQuizAnswer = new QuizAnswerTCC((QuizAnswerTCC) oldQuizAnswer);

        if (!newQuizAnswer.getAnswerCourseExecution().getCourseExecutionAggregateId().equals(executionAggregateId)) {
            return;
        }

        if (newQuizAnswer.getStudent().getStudentAggregateId().equals(userAggregateId)) {
            newQuizAnswer.getStudent().setName(name);
            newQuizAnswer.getAnswerCourseExecution().setCourseExecutionVersion(eventVersion);
            unitOfWork.registerChanged(newQuizAnswer);
        }
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizAnswer removeUser(Integer answerAggregateId, Integer userAggregateId, Integer aggregateVersion, UnitOfWork unitOfWork) {
        QuizAnswer oldQuizAnswer = (QuizAnswer) unitOfWorkService.aggregateLoadAndRegisterRead(answerAggregateId, unitOfWork);
        if (oldQuizAnswer != null && oldQuizAnswer.getStudent().getStudentAggregateId().equals(userAggregateId) && oldQuizAnswer.getVersion() >= aggregateVersion) {
            return null;
        }

        QuizAnswer newQuizAnswer = new QuizAnswerTCC((QuizAnswerTCC) oldQuizAnswer);
        newQuizAnswer.getStudent().setStudentState(Aggregate.AggregateState.DELETED);
        newQuizAnswer.setState(Aggregate.AggregateState.INACTIVE);
        unitOfWork.registerChanged(newQuizAnswer);
        return newQuizAnswer;
    }

    public QuizAnswer removeQuestion(Integer answerAggregateId, Integer questionAggregateId, Integer aggregateVersion, UnitOfWork unitOfWork) {
        QuizAnswer oldQuizAnswer = (QuizAnswer) unitOfWorkService.aggregateLoadAndRegisterRead(answerAggregateId, unitOfWork);
        QuestionAnswer questionAnswer = oldQuizAnswer.findQuestionAnswer(questionAggregateId);

        if (questionAnswer == null) {
            return null;
        }

        QuizAnswer newQuizAnswer = new QuizAnswerTCC((QuizAnswerTCC) oldQuizAnswer);
        questionAnswer.setState(Aggregate.AggregateState.DELETED);
        newQuizAnswer.setState(Aggregate.AggregateState.INACTIVE);
        unitOfWork.registerChanged(newQuizAnswer);
        return newQuizAnswer;
    }
}
