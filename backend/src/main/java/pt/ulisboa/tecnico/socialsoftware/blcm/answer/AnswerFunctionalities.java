package pt.ulisboa.tecnico.socialsoftware.blcm.answer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto.QuestionAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.service.AnswerService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.domain.QuestionTopic;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.service.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;

@Service
public class AnswerFunctionalities {

    @Autowired
    private QuizService quizService;

     @Autowired
     private AnswerService answerService;

     @Autowired
     private QuestionService questionService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;



    public void answerQuestion(Integer quizAggregateId, Integer userAggregateId, QuestionAnswerDto userQuestionAnswerDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        QuestionDto questionDto = questionService.getCausalQuestionRemote(userQuestionAnswerDto.getQuestionAggregateId(), unitOfWork);
        answerService.answerQuestion(quizAggregateId, userAggregateId, userQuestionAnswerDto, questionDto, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void startQuiz(Integer quizAggregateId) {

    }

    public void concludeQuiz(Integer quizAggregateId) {

    }

    public void getSolvedQuizzes(Integer userAggregateId, Integer courseExecutionAggregateId) {

    }

}
