package pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.course.CourseType;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class ExecutionCourse {

    @Column(name = "course_aggregate_id")
    private Integer courseAggregateId;

    private String name;

    @Enumerated(EnumType.STRING)
    private CourseType type;

    @Column(name = "course_version")
    private Integer version;

    public ExecutionCourse() {}

    public ExecutionCourse(CourseExecutionDto courseExecutionDto) {
        setAggregateId(courseExecutionDto.getCourseAggregateId());
        setName(courseExecutionDto.getName());
        setType(CourseType.valueOf(courseExecutionDto.getType()));
        setVersion(courseExecutionDto.getVersion());
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
