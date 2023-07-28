package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.domain;

import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/*
    INTRA-INVARIANTS:

    INTER-INVARIANTS:
        COURSE-EXISTS (course doesnt send events)
 */
@Entity
public abstract class Topic extends Aggregate {
    @Column
    private String name;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "topic")
    private TopicCourse topicCourse;

    public Topic() {}

    public Topic(Integer aggregateId, String name, TopicCourse topicCourse) {
        super(aggregateId);
        setAggregateType(getClass().getSimpleName());
        setName(name);
        setTopicCourse(topicCourse);
    }

    public Topic(Topic other) {
        super(other);
        setName(other.getName());
        setTopicCourse(new TopicCourse(other.getTopicCourse()));
    }

    @Override
    public void verifyInvariants() {
    }

    @Override
    public Set<EventSubscription> getEventSubscriptions() {
        return new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TopicCourse getTopicCourse() {
        return topicCourse;
    }

    public void setTopicCourse(TopicCourse course) {
        this.topicCourse = course;
        this.topicCourse.setTopic(this);
    }

}
