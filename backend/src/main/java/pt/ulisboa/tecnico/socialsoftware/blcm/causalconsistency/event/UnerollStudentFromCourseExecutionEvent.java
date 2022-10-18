package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.ANONYMIZE_EXECUTION_STUDENT;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.UNENROLL_STUDENT;

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
