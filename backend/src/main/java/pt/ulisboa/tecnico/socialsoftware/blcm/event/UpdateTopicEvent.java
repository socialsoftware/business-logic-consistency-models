package pt.ulisboa.tecnico.socialsoftware.blcm.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.Topic;

public class UpdateTopicEvent extends DomainEvent {
    private Integer aggregateId;

    private String name;

    public UpdateTopicEvent(Topic topic) {
        setAggregateId(topic.getAggregateId());
        setName(topic.getName());
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
