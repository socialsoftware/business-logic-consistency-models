package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.Topic;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
public class DeleteTopicEvent extends Event {



    public DeleteTopicEvent() {
        super();
    }

    public DeleteTopicEvent(Topic topic) {
        super(topic.getAggregateId());
    }
}
