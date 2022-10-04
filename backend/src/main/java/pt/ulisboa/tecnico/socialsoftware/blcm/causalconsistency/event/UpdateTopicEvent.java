package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.Topic;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.UPDATE_TOPIC;

@Entity
@DiscriminatorValue(UPDATE_TOPIC)
public class UpdateTopicEvent extends DomainEvent {
    private Integer topicAggregateId;

    private String topicName;

    public UpdateTopicEvent() {

    }

    public UpdateTopicEvent(Topic topic) {
        super();
        setTopicAggregateId(topic.getAggregateId());
        setTopicName(topic.getName());
    }



    public Integer getTopicAggregateId() {
        return topicAggregateId;
    }

    public void setTopicAggregateId(Integer aggregateId) {
        this.topicAggregateId = aggregateId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String name) {
        this.topicName = name;
    }
}
