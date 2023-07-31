package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.dto.QuestionAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.service.QuizAnswerService;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.service.QuestionService;

@Service
public class QuizAnswerFunctionalities {
     @Autowired
     private QuizAnswerService quizAnswerService;
     @Autowired
     private QuestionService questionService;
    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public void answerQuestion(Integer quizAggregateId, Integer userAggregateId, QuestionAnswerDto userQuestionAnswerDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        QuestionDto questionDto = questionService.getQuestionById(userQuestionAnswerDto.getQuestionAggregateId(), unitOfWork);
        quizAnswerService.answerQuestion(quizAggregateId, userAggregateId, userQuestionAnswerDto, questionDto, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void startQuiz(Integer quizAggregateId, Integer courseExecutionAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        quizAnswerService.startQuiz(quizAggregateId, courseExecutionAggregateId, userAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void concludeQuiz(Integer quizAggregateId, Integer courseExecutionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        quizAnswerService.concludeQuiz(quizAggregateId, courseExecutionAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
}
