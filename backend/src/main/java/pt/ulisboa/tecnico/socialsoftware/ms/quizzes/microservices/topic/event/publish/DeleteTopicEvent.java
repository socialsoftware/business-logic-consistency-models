package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.event.publish;

import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.Event;

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
