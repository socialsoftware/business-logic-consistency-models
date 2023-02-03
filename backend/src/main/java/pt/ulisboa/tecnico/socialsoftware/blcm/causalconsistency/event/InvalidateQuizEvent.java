package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import javax.persistence.Entity;

@Entity
public class InvalidateQuizEvent extends Event {

    public InvalidateQuizEvent() {
        super();
    }

    public InvalidateQuizEvent(Integer aggregateId) {
        super(aggregateId);
    }
}
