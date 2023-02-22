package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.Event;

@Entity
public class InvalidateQuizEvent extends Event {

    public InvalidateQuizEvent() {
        super();
    }

    public InvalidateQuizEvent(Integer aggregateId) {
        super(aggregateId);
    }
}
