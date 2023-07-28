package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.event.publish;

import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;

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
