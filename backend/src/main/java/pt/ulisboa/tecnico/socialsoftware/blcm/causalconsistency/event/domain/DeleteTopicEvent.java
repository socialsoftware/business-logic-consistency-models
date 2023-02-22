package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.Topic;

import jakarta.persistence.Entity;

@Entity
public class DeleteTopicEvent extends Event {



    public DeleteTopicEvent() {
        super();
    }

    public DeleteTopicEvent(Topic topic) {
        super(topic.getAggregateId());
    }
}
