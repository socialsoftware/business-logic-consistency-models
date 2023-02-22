package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import jakarta.persistence.*;

@Embeddable
public class AnswerCourseExecution {
    private Integer courseExecutionAggregateId;
    private Integer courseExecutionVersion;

    public AnswerCourseExecution() {
        this.courseExecutionAggregateId = 0;
    }

    public AnswerCourseExecution(Integer courseExecutionAggregateId, Integer courseExecutionVersion) {
        this.courseExecutionAggregateId = courseExecutionAggregateId;
        setCourseExecutionVersion(courseExecutionVersion);
    }

    public AnswerCourseExecution(AnswerCourseExecution courseExecution) {
        this.courseExecutionAggregateId = courseExecution.getCourseExecutionAggregateId();
        setCourseExecutionVersion(courseExecution.getCourseExecutionVersion());
    }

    public Integer getCourseExecutionAggregateId() {
        return courseExecutionAggregateId;
    }

    public void setCourseExecutionAggregateId(Integer courseExecutionAggregateId) {
        this.courseExecutionAggregateId = courseExecutionAggregateId;
    }

    public Integer getCourseExecutionVersion() {
        return courseExecutionVersion;
    }

    public void setCourseExecutionVersion(Integer courseExecutionVersion) {
        this.courseExecutionVersion = courseExecutionVersion;
    }
}
