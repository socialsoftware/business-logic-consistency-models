package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;

@Embeddable
public class AnswerQuiz {
    @Column(name = "quiz_aggregate_id")
    private Integer aggregateId;

    @Column(name = "quiz_version")
    private Integer version;

    @ElementCollection
    private List<Integer> quizQuestionsAggregateIds;

    public AnswerQuiz() {

    }

    public AnswerQuiz(QuizDto quizDto) {
        setAggregateId(quizDto.getAggregateId());
        setVersion(quizDto.getVersion());
        setQuizQuestionsAggregateIds(quizDto.getQuestionsAggregateIds());
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
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
