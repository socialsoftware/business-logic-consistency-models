package pt.ulisboa.tecnico.socialsoftware.blcm.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.Topic;

public class DeleteTopicEvent extends DomainEvent {

    private Integer aggregateId;

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
