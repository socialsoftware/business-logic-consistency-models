package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import javax.persistence.Entity;

@Entity
public class RemoveCourseExecutionEvent extends Event {

    public RemoveCourseExecutionEvent() {
        super();
    }

    public RemoveCourseExecutionEvent(Integer aggregateId) {
        super(aggregateId);
    }
}
