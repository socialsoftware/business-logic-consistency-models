package pt.ulisboa.tecnico.socialsoftware.blcm.course.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.COURSE;

/*
    INTRA-INVARIANTS:
        COURSE_TYPE_FINAL
        COURSE_NAME_FINAL
    INTER_INVARIANTS:
 */
@Entity
@Table(name = "courses")
public class Course extends Aggregate {
    /*
        COURSE_TYPE_FINAL
     */
    @Enumerated(EnumType.STRING)
    private final CourseType type;

    /*
        COURSE_NAME_FINAL
     */
    @Column
    private final String name;

    public Course() {
        this.name = "COURSE NAME";
        this.type = CourseType.TECNICO;
    }

    public Course(Integer aggregateId, CourseExecutionDto courseExecutionDto) {
        super(aggregateId, COURSE);
        this.name = courseExecutionDto.getName();
        this.type = CourseType.valueOf(courseExecutionDto.getType());
    }

    public Course(Course other) {
        super(other.getAggregateId(), COURSE);
        setId(null);
        this.name = other.getName();
        this.type = other.getType();
        setPrev(other);
        setProcessedEvents(new HashMap<>(other.getProcessedEvents()));
        setEmittedEvents(new HashMap<>(other.getEmittedEvents()));
    }

    @Override
    public Set<String> getEventSubscriptions() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getFieldsAbleToChange() {
        return null;
    }

    @Override
    public Set<String> getIntentionFields() {
        return null;
    }

    @Override
    public Aggregate mergeFields(Set<String> toCommitVersionChangedFields, Aggregate committedVersion, Set<String> committedVersionChangedFields) {
        return null;
    }

    @Override
    public void verifyInvariants() {

    }

    @Override
    public Aggregate merge(Aggregate other) {
        return this;
    }

    public CourseType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
