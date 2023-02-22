package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AnswerCourseExecution {
    @Column(name = "answer_course_execution_aggregate_id")
    private final Integer aggregateId;
    @Column(name = "answer_course_execution_aggregate_version")
    private Integer version;

    public AnswerCourseExecution() {
        this.aggregateId = 0;
    }

    public AnswerCourseExecution(Integer courseExecutionAggregateId, Integer courseExecutionVersion) {
        this.aggregateId = courseExecutionAggregateId;
        setVersion(courseExecutionVersion);
    }

    public AnswerCourseExecution(AnswerCourseExecution courseExecution) {
        this.aggregateId = courseExecution.getAggregateId();
        setVersion(courseExecution.getVersion());
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
}
