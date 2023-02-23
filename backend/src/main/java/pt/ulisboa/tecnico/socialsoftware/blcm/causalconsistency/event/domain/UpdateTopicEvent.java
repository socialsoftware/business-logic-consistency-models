package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.Topic;

import jakarta.persistence.Entity;

@Entity
public class UpdateTopicEvent extends Event {

    private String topicName;

    public UpdateTopicEvent() {
        super();
    }

    public UpdateTopicEvent(Topic topic) {
        super(topic.getAggregateId());
        setTopicName(topic.getName());
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String name) {
        this.topicName = name;
    }
}