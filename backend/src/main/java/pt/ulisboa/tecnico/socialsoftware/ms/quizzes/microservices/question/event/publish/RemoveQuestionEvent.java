package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.question.event.publish;

import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.Event;

import jakarta.persistence.Entity;

@Entity
public class RemoveQuestionEvent extends Event {
    public RemoveQuestionEvent() {
        super();
    }

    public RemoveQuestionEvent(Integer questionAggregateId) {
        super(questionAggregateId);
    }
}
