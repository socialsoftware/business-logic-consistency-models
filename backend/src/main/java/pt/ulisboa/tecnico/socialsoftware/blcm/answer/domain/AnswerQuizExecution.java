package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class AnswerQuizExecution {
    @Column(name = "quiz_course_execution_aggregate_id")
    private Integer aggregateId;

    @Column(name = "quiz_course_execution_aggregate_version")
    private Integer version;

    public AnswerQuizExecution(Integer courseExecutionAggregateId, Integer courseExecutionVersion) {
        setAggregateId(courseExecutionAggregateId);
        setVersion(courseExecutionVersion);
    }

    public AnswerQuizExecution(AnswerQuizExecution courseExecution) {
        setAggregateId(courseExecution.getAggregateId());
        setVersion(courseExecution.getVersion());
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
