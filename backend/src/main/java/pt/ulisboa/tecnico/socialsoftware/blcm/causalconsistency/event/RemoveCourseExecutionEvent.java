package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

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
