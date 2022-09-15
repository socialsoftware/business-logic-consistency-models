package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.Topic;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("UPDATE_TOPIC")
public class UpdateTopicEvent extends DomainEvent {
    private Integer aggregateId;

    private String name;

    public UpdateTopicEvent() {

    }

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
