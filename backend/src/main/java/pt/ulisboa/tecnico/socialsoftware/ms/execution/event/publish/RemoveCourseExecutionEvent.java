package pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.Event;

@Entity
public class RemoveCourseExecutionEvent extends Event {

    public RemoveCourseExecutionEvent() {}

    public RemoveCourseExecutionEvent(Integer aggregateId) {
        super(aggregateId);
    }
}
