package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.dto.QuestionAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.QuizAnswerFunctionalities;

@RestController
public class QuizAnswerController {

    @Autowired
    private QuizAnswerFunctionalities quizAnswerFunctionalities;

    @PostMapping("/quizzes/{quizAggregateId}/answer")
    public void answerQuestion(@PathVariable Integer quizAggregateId, @RequestParam Integer userAggregateId, @RequestBody QuestionAnswerDto questionAnswerDto) {
        quizAnswerFunctionalities.answerQuestion(quizAggregateId, userAggregateId, questionAnswerDto);
    }
}
