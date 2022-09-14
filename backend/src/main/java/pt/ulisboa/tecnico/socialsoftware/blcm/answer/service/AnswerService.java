package pt.ulisboa.tecnico.socialsoftware.blcm.answer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.AnswerQuiz;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.AnswerUser;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto.QuestionAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto.QuizAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.repository.AnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.service.UserService;

import javax.transaction.Transactional;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.DELETED;
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

    @Transactional
    public QuizAnswerDto getCausalQuizAnswerRemove(Integer aggregateId, UnitOfWork unitOfWork) {
        return new QuizAnswerDto(getCausalQuizAnswerLocal(aggregateId, unitOfWork));
    }

    public QuizAnswer getCausalQuizAnswerLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        QuizAnswer quizAnswer = answerRepository.findByAggregateIdAndVersion(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(QUIZ_ANSWER_NOT_FOUND, aggregateId));

        if(quizAnswer.getState().equals(DELETED)) {
            throw new TutorException(ErrorMessage.QUIZ_ANSWER_DELETED, quizAnswer.getAggregateId());
        }

        unitOfWork.checkDependencies(quizAnswer);
        return quizAnswer;
    }

    public QuizAnswer getCausalQuizAnswerLocalByQuizAndUser(Integer quizAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        QuizAnswer quizAnswer = answerRepository.findByQuizUserAndVersion(quizAggregateId, userAggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(NO_USER_ANSWER_FOR_QUIZ, quizAggregateId, userAggregateId));

        if(quizAnswer.getState().equals(DELETED)) {
            throw new TutorException(ErrorMessage.QUIZ_ANSWER_DELETED, quizAnswer.getAggregateId());
        }

        unitOfWork.checkDependencies(quizAnswer);
        return quizAnswer;
    }



    @Transactional
    public void startQuiz(Integer quizAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        QuizDto quizDto = quizService.getCausalQuizRemote(quizAggregateId, unitOfWork);
        UserDto userDto = userService.getCausalUserRemote(userAggregateId, unitOfWork);

        QuizAnswer quizAnswer = new QuizAnswer(aggregateId, new AnswerUser(userDto), new AnswerQuiz(quizDto));

        unitOfWork.addUpdatedObject(quizAnswer);
    }

    @Transactional
    public void answerQuestion(Integer quizAggregateId, Integer userAggregateId, QuestionAnswerDto questionAnswerDto, UnitOfWork unitOfWork) {
        QuizAnswer oldQuizAnswer = getCausalQuizAnswerLocalByQuizAndUser(quizAggregateId, userAggregateId, unitOfWork);
        QuizAnswer newQuizAnswer = new QuizAnswer(oldQuizAnswer);

        QuestionAnswer questionAnswer = new QuestionAnswer(questionAnswerDto);
        newQuizAnswer.addQuestionAnswer(questionAnswer);
        unitOfWork.addUpdatedObject(newQuizAnswer);
    }


    @Transactional
    public void concludeQuiz(Integer quizAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        QuizAnswer oldQuizAnswer = getCausalQuizAnswerLocalByQuizAndUser(quizAggregateId, userAggregateId, unitOfWork);
        QuizAnswer newQuizAnswer = new QuizAnswer(oldQuizAnswer);

        newQuizAnswer.setCompleted(true);
        unitOfWork.addUpdatedObject(newQuizAnswer);
    }
}
