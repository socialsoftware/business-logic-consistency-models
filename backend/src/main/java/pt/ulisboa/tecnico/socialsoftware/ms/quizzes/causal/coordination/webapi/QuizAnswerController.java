package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.causal.coordination.webapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.causal.coordination.functionalities.QuizAnswerFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.answer.aggregate.QuestionAnswerDto;

@RestController
public class QuizAnswerController {

    @Autowired
    private QuizAnswerFunctionalities quizAnswerFunctionalities;

    @PostMapping("/quizzes/{quizAggregateId}/answer")
    public void answerQuestion(@PathVariable Integer quizAggregateId, @RequestParam Integer userAggregateId, @RequestBody QuestionAnswerDto questionAnswerDto) {
        quizAnswerFunctionalities.answerQuestion(quizAggregateId, userAggregateId, questionAnswerDto);
    }
}
