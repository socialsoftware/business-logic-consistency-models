package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class QuizCourseExecution {
    @Column(name = "course_execution_aggregate_id")
    private Integer aggregateId;

    @Column(name = "course_execution_version")
    private Integer version;

    public QuizCourseExecution() {

    }

    public QuizCourseExecution(CourseExecutionDto courseExecutionDto) {
        setAggregateId(courseExecutionDto.getAggregateId());
        setVersion(courseExecutionDto.getVersion());
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
