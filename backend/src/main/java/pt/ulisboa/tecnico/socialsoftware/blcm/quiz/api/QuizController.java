package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizFunctionalities;

@RestController
public class QuizController {

    @Autowired
    private QuizFunctionalities quizFunctionalities;

    @PostMapping("/executions/{courseExecutionId}")
    public QuizDto createQuiz(@PathVariable Integer courseExecutionId, @RequestBody QuizDto quizDto) {
        return quizFunctionalities.createQuiz(courseExecutionId, quizDto);
    }
    @PostMapping("/quizzes/update")
    public QuizDto updateQuiz(@RequestBody QuizDto quizDto) {
        return quizFunctionalities.updateQuiz(quizDto);
    }
}
