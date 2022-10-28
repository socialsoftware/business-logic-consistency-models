package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Embeddable
public class AnswerQuiz {
    @Column(name = "quiz_aggregate_id")
    private final Integer aggregateId;

    @Column(name = "quiz_version")
    private Integer version;

    @ElementCollection
    private List<Integer> quizQuestionsAggregateIds;

    public AnswerQuiz() {
        this.aggregateId = 0;
    }

    public AnswerQuiz(QuizDto quizDto) {
        this.aggregateId = quizDto.getAggregateId();
        setVersion(quizDto.getVersion());
        setQuizQuestionsAggregateIds(quizDto.getQuestionDtos().stream()
                .map(QuestionDto::getAggregateId)
                .collect(Collectors.toList()));
    }

    public AnswerQuiz(AnswerQuiz other) {
        this.aggregateId = other.getAggregateId();
        setVersion(other.getVersion());
        setQuizQuestionsAggregateIds(new ArrayList<>(other.getQuizQuestionsAggregateIds()));
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<Integer> getQuizQuestionsAggregateIds() {
        return quizQuestionsAggregateIds;
    }

    public void setQuizQuestionsAggregateIds(List<Integer> quizQuestionsAggregateIds) {
        this.quizQuestionsAggregateIds = quizQuestionsAggregateIds;
    }
}
