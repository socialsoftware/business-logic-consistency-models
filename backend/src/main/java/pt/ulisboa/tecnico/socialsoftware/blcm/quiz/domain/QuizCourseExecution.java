package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

@Embeddable
public class QuizCourseExecution {
    private Integer courseExecutionAggregateId;
    private Integer courseExecutionVersion;

    public QuizCourseExecution() {

    }

    public QuizCourseExecution(CourseExecutionDto courseExecutionDto) {
        setCourseExecutionAggregateId(courseExecutionDto.getAggregateId());
        setCourseExecutionVersion(courseExecutionDto.getVersion());
    }

    public QuizCourseExecution(QuizCourseExecution other) {
        setCourseExecutionAggregateId(other.getCourseExecutionAggregateId());
        setCourseExecutionVersion(other.getCourseExecutionVersion());
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
