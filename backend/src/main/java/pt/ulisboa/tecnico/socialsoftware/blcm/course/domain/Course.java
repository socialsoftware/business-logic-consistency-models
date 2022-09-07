package pt.ulisboa.tecnico.socialsoftware.blcm.course.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.Dependency;

import javax.persistence.*;
import java.util.Map;

@Entity
@Table(name = "courses")
public class Course extends Aggregate {

    @ManyToOne(fetch = FetchType.LAZY)
    private Course prev;

    @Enumerated(EnumType.STRING)
    private CourseType type;

    @Column
    private String name;

    public Course() {

    }

    public Course(Integer aggregateId, Integer version, CourseExecutionDto courseExecutionDto) {
        super(aggregateId);
        setName(courseExecutionDto.getName());
        setType(CourseType.valueOf(courseExecutionDto.getType()));
    }

    public Course(Course other) {
        super(other.getAggregateId());
        setName(other.getName());
        setType(other.getType());
        setPrev(other);
    }

    @Override
    public boolean verifyInvariants() {
        return true;
    }

    public static Course merge(Course prev, Course v1, Course v2) {
        // choose the object with lowest ts
        if(v2.getCreationTs().isBefore(v1.getCreationTs())) {
            return v2;
        } else {
            return v1;
        }
    }

    @Override
    public Aggregate getPrev() {
        return this.prev;
    }

    @Override
    public Aggregate merge(Aggregate other) {
        return this;
    }

    @Override
    public Map<Integer, Dependency> getDependenciesMap() {
        return null;
    }

    public void setPrev(Course prev) {
        this.prev = prev;
    }

    public CourseType getType() {
        return type;
    }

    public void setType(CourseType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
