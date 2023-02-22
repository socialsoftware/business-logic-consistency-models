package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.Event;

@Entity
public class RemoveCourseExecutionEvent extends Event {

    public RemoveCourseExecutionEvent() {
        super();
    }

    public RemoveCourseExecutionEvent(Integer aggregateId) {
        super(aggregateId);
    }
}
