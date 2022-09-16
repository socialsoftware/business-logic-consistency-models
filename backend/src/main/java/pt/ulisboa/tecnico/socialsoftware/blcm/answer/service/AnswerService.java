package pt.ulisboa.tecnico.socialsoftware.blcm.answer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.Answer;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.AnswerQuiz;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.AnswerUser;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto.QuestionAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto.QuizAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.repository.AnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.service.UserService;

import java.sql.SQLException;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.NO_USER_ANSWER_FOR_QUIZ;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.QUIZ_ANSWER_NOT_FOUND;

@Service
public class AnswerService {

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserService userService;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizAnswerDto getCausalQuizAnswerRemove(Integer aggregateId, UnitOfWork unitOfWork) {
        return new QuizAnswerDto(getCausalQuizAnswerLocal(aggregateId, unitOfWork));
    }

    public Answer getCausalQuizAnswerLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        Answer answer = answerRepository.findCausal(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(QUIZ_ANSWER_NOT_FOUND, aggregateId));

        if(answer.getState().equals(DELETED)) {
            throw new TutorException(ErrorMessage.QUIZ_ANSWER_DELETED, answer.getAggregateId());
        }

        unitOfWork.addToCausalSnapshot(answer);
        return answer;
    }

    public Answer getCausalQuizAnswerLocalByQuizAndUser(Integer quizAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        Answer answer = answerRepository.findCausalByQuizAndUser(quizAggregateId, userAggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(NO_USER_ANSWER_FOR_QUIZ, quizAggregateId, userAggregateId));

        if(answer.getState().equals(DELETED)) {
            throw new TutorException(ErrorMessage.QUIZ_ANSWER_DELETED, answer.getAggregateId());
        }

        unitOfWork.addToCausalSnapshot(answer);
        return answer;
    }



    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void startQuiz(Integer quizAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        QuizDto quizDto = quizService.getCausalQuizRemote(quizAggregateId, unitOfWork);
        UserDto userDto = userService.getCausalUserRemote(userAggregateId, unitOfWork);

        Answer answer = new Answer(aggregateId, new AnswerUser(userDto), new AnswerQuiz(quizDto));

        unitOfWork.addUpdatedObject(answer);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void answerQuestion(Integer quizAggregateId, Integer userAggregateId, QuestionAnswerDto questionAnswerDto, UnitOfWork unitOfWork) {
        Answer oldAnswer = getCausalQuizAnswerLocalByQuizAndUser(quizAggregateId, userAggregateId, unitOfWork);
        Answer newAnswer = new Answer(oldAnswer);

        QuestionAnswer questionAnswer = new QuestionAnswer(questionAnswerDto);
        newAnswer.addQuestionAnswer(questionAnswer);
        unitOfWork.addUpdatedObject(newAnswer);
    }


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void concludeQuiz(Integer quizAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        Answer oldAnswer = getCausalQuizAnswerLocalByQuizAndUser(quizAggregateId, userAggregateId, unitOfWork);
        Answer newAnswer = new Answer(oldAnswer);

        newAnswer.setCompleted(true);
        newAnswer.getQuestionAnswers().forEach(qa -> {
            // TODO check whether answers are correct or wrong by making a request to each question
        });
        unitOfWork.addUpdatedObject(newAnswer);
    }
}
