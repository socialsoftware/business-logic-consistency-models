package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.Topic;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("DELETE_TOPIC")
public class DeleteTopicEvent extends DomainEvent {



    public DeleteTopicEvent() {
        super();
    }

    public DeleteTopicEvent(Topic topic) {
        super(topic.getAggregateId());
    }
}
