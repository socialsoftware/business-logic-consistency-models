package pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.EventualConsistencyDependency;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.COURSE;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.TOPIC;

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
    }


    public static Topic merge(Topic prev, Topic v1, Topic v2) {
        // choose the object with lowest ts
        if(v2.getCreationTs().isBefore(v1.getCreationTs())) {
            return v2;
        } else {
            return v1;
        }

    }
    @Override
    public boolean verifyInvariants() {
        return true;
    }

    @Override
    public Aggregate merge(Aggregate other) {
        return this;
    }

    @Override
    public Map<Integer, EventualConsistencyDependency> getDependenciesMap() {
        Map<Integer, EventualConsistencyDependency> depMap = new HashMap<>();
        depMap.put(this.course.getAggregateId(), new EventualConsistencyDependency(this.course.getAggregateId(), COURSE ,this.course.getVersion()));
        return depMap;
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
