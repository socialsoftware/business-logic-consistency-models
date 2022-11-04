package pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateComponent;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.domain.CourseType;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

import javax.persistence.*;

@Entity
public class ExecutionCourse extends AggregateComponent {

    @Column
    private String name;

    @Enumerated(EnumType.STRING)
    private CourseType type;

    public ExecutionCourse() {
        super();
    }

    public ExecutionCourse(CourseExecutionDto courseExecutionDto) {
        super(courseExecutionDto.getCourseAggregateId(), courseExecutionDto.getCourseVersion());
        setName(courseExecutionDto.getName());
        setType(CourseType.valueOf(courseExecutionDto.getType()));
    }

    public ExecutionCourse(ExecutionCourse other) {
        super(other.getAggregateId(), other.getVersion());
        setName(other.getName());
        setType(other.getType());
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
