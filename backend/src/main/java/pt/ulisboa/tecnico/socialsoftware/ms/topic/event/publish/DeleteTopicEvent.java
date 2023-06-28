package pt.ulisboa.tecnico.socialsoftware.ms.topic.event.publish;

import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.Event;

import jakarta.persistence.Entity;

@Entity
public class DeleteTopicEvent extends Event {
    public DeleteTopicEvent() {
        super();
    }
    public DeleteTopicEvent(Integer topicAggregateId) {
        super(topicAggregateId);
    }
}
