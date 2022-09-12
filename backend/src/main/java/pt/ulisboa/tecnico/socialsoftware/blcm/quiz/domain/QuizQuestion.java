package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class QuizQuestion {
    @Column(name = "question_aggregate_id")
    private Integer aggregateId;

    @Column(name = "question_version")
    private Integer version;

    private String title;

    private String content;

    private Integer sequence;


    public QuizQuestion() {

    }


    public QuizQuestion(QuestionDto questionDto) {
        setAggregateId(questionDto.getAggregateId());
        setVersion(questionDto.getVersion());
        setTitle(questionDto.getTitle());
        setContent(questionDto.getContent());
        setSequence(questionDto.getSequence());
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }
}
