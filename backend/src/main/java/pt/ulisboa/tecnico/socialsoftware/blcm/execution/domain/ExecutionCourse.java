package pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.course.CourseType;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class ExecutionCourse {

    private Integer courseAggregateId;

    private String name;

    @Enumerated(EnumType.STRING)
    private CourseType type;

    public ExecutionCourse() {}

    public ExecutionCourse(CourseExecutionDto courseExecutionDto) {
        setAggregateId(courseExecutionDto.getCourseAggregateId());
        setName(courseExecutionDto.getName());
        setType(CourseType.valueOf(courseExecutionDto.getType()));
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
}
