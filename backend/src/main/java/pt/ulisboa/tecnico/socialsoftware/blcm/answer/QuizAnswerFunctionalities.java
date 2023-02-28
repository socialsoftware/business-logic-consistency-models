package pt.ulisboa.tecnico.socialsoftware.blcm.answer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto.QuestionAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.service.QuizAnswerService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.service.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;

@Service
public class QuizAnswerFunctionalities {
    @Autowired
    private QuizService quizService;

     @Autowired
     private QuizAnswerService quizAnswerService;

     @Autowired
     private QuestionService questionService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;


    public void answerQuestion(Integer quizAggregateId, Integer userAggregateId, QuestionAnswerDto userQuestionAnswerDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        QuestionDto questionDto = questionService.getCausalQuestionRemote(userQuestionAnswerDto.getQuestionAggregateId(), unitOfWork);
        quizAnswerService.answerQuestion(quizAggregateId, userAggregateId, userQuestionAnswerDto, questionDto, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void startQuiz(Integer quizAggregateId, Integer courseExecutionAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        quizAnswerService.startQuiz(quizAggregateId, courseExecutionAggregateId, userAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void concludeQuiz(Integer quizAggregateId, Integer courseExecutionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        quizAnswerService.concludeQuiz(quizAggregateId, courseExecutionAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
}
