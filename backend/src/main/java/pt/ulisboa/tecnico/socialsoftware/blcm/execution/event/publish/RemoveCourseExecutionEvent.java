package pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;

@Entity
public class RemoveCourseExecutionEvent extends Event {

    public RemoveCourseExecutionEvent() {}

    public RemoveCourseExecutionEvent(Integer aggregateId) {
        super(aggregateId);
    }
}
