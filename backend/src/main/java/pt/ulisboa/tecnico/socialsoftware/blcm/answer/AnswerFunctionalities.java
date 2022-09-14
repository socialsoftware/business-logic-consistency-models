package pt.ulisboa.tecnico.socialsoftware.blcm.answer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;

@Service
public class AnswerFunctionalities {

    @Autowired
    private QuizService quizService;



    public void answerQuestion(Integer quizAggregateId, Integer userAggregateId, Integer questionAggregateId) {

    }

    public void concludeQuiz(Integer quizAggregateId) {

    }


}
