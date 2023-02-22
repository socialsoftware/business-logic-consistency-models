package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.domain.Question;

import jakarta.persistence.Entity;

@Entity
public class RemoveQuestionEvent extends Event {

    public RemoveQuestionEvent() {
        super();
    }

    public RemoveQuestionEvent(Question question) {
        super(question.getAggregateId());
    }
}
