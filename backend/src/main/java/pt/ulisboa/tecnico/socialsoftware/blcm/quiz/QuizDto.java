package pt.ulisboa.tecnico.socialsoftware.blcm.quiz;

import java.io.Serializable;

public class QuizDto implements Serializable {
    private Integer aggregateId;

    private Integer version;

    public QuizDto() {

    }

    public QuizDto(Quiz quiz) {

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
}
