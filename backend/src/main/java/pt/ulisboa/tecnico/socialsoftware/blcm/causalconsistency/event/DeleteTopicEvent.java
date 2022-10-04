package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.Topic;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("DELETE_TOPIC")
public class DeleteTopicEvent extends DomainEvent {

    private Integer topicAggregateId;


    public DeleteTopicEvent() {
        super();
    }

    public DeleteTopicEvent(Topic topic) {
        super();
       setTopicAggregateId(topic.getAggregateId());
    }

    public Integer getTopicAggregateId() {
        return topicAggregateId;
    }

    public void setTopicAggregateId(Integer aggregateId) {
        this.topicAggregateId = aggregateId;
    }

}
