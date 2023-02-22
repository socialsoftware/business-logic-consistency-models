package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.Event;

@Entity
public class UnerollStudentFromCourseExecutionEvent extends Event {

    private Integer userAggregateId;

    public UnerollStudentFromCourseExecutionEvent() {
        super();
    }

    public UnerollStudentFromCourseExecutionEvent(Integer courseExecutionAggregateId, Integer userAggregateId) {
        super(courseExecutionAggregateId);
        setUserAggregateId(userAggregateId);
    }



    public Integer getUserAggregateId() {
        return userAggregateId;
    }

    public void setUserAggregateId(Integer userAggregateId) {
        this.userAggregateId = userAggregateId;
    }
}
