package pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.domain.CourseType;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

@Embeddable
public class ExecutionCourse {
    private Integer courseAggregateId;
    private String name;
    @Enumerated(EnumType.STRING)
    private CourseType type;
    private Integer courseVersion;

    public ExecutionCourse() {}

    public ExecutionCourse(CourseExecutionDto courseExecutionDto) {
        setAggregateId(courseExecutionDto.getCourseAggregateId());
        setName(courseExecutionDto.getName());
        setType(CourseType.valueOf(courseExecutionDto.getType()));
        setCourseVersion(courseExecutionDto.getCourseVersion());
    }

    public Integer getAggregateId() {
        return courseAggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.courseAggregateId = aggregateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CourseType getType() {
        return type;
    }

    public void setType(CourseType type) {
        this.type = type;
    }

    public Integer getCourseVersion() {
        return courseVersion;
    }

    public void setCourseVersion(Integer courseVersion) {
        this.courseVersion = courseVersion;
    }
}
