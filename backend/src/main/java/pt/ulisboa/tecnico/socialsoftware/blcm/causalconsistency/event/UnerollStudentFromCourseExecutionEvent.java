package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.UNENROLL_STUDENT;

@Entity
@DiscriminatorValue(UNENROLL_STUDENT)
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
