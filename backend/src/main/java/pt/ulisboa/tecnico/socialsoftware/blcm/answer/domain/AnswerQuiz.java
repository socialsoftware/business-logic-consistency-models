package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateComponent;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class AnswerQuiz extends AggregateComponent {

    @ElementCollection
    private List<Integer> quizQuestionsAggregateIds;

    public AnswerQuiz() {
        super();
    }

    public AnswerQuiz(QuizDto quizDto) {
        super(quizDto.getAggregateId(), quizDto.getVersion());
        setQuizQuestionsAggregateIds(quizDto.getQuestionDtos().stream()
                .map(QuestionDto::getAggregateId)
                .collect(Collectors.toList()));
    }

    public AnswerQuiz(AnswerQuiz other) {
        super(other.getAggregateId(), other.getVersion());
        setQuizQuestionsAggregateIds(new ArrayList<>(other.getQuizQuestionsAggregateIds()));
    }

    public List<Integer> getQuizQuestionsAggregateIds() {
        return quizQuestionsAggregateIds;
    }

    public void setQuizQuestionsAggregateIds(List<Integer> quizQuestionsAggregateIds) {
        this.quizQuestionsAggregateIds = quizQuestionsAggregateIds;
    }
}
