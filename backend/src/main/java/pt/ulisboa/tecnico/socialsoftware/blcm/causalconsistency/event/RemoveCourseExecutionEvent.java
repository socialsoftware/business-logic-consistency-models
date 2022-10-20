package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(EventType.REMOVE_COURSE_EXECUTION)
public class RemoveCourseExecutionEvent extends Event {

    public RemoveCourseExecutionEvent() {
        super();
    }

    public RemoveCourseExecutionEvent(Integer aggregateId) {
        super(aggregateId);
    }
}
