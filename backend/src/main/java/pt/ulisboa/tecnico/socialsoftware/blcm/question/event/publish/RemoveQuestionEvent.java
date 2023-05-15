package pt.ulisboa.tecnico.socialsoftware.blcm.question.event.publish;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;

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
