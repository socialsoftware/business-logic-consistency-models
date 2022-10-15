package pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.TOPIC;

/*
    INTRA-INVARIANTS:

    INTER-INVARIANTS:
        COURSE-EXISTS (course doesnt send events)
 */
@Entity
@Table(name = "topics")
public class Topic extends Aggregate {

    @Column
    private String name;

    @Embedded
    private TopicCourse course;

    public Topic() {}

    public Topic(Integer aggregateId, String name, TopicCourse course) {
        super(aggregateId, TOPIC);
        setName(name);
        setCourse(course);
    }

    public Topic(Topic other) {
        super(other.getAggregateId(), TOPIC);
        setName(other.getName());
        setCourse(other.getCourse());
        setProcessedEvents(new HashMap<>(other.getProcessedEvents()));
        setEmittedEvents(new HashMap<>(other.getEmittedEvents()));
        setPrev(other);
    }

    @Override
    public void verifyInvariants() {

    }

    @Override
    public Aggregate merge(Aggregate other) {
        return this;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TopicCourse getCourse() {
        return course;
    }

    public void setCourse(TopicCourse course) {
        this.course = course;
    }

}
