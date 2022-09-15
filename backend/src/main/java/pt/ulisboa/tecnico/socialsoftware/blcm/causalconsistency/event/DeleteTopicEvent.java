package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.Topic;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("DELETE_TOPIC")
public class DeleteTopicEvent extends DomainEvent {

    private Integer aggregateId;

    public DeleteTopicEvent() {

    }

    public DeleteTopicEvent(Topic topic) {
       setAggregateId(topic.getAggregateId());
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }
}
