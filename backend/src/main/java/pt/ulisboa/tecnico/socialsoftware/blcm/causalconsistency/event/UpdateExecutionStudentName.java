package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.UNENROLL_STUDENT;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.UPDATE_EXECUTION_STUDENT_NAME;

@Entity
@DiscriminatorValue(UPDATE_EXECUTION_STUDENT_NAME)
public class UpdateExecutionStudentName extends Event{

    private Integer userAggregateId;
    private String name;

    public UpdateExecutionStudentName() {

    }

    public UpdateExecutionStudentName(Integer executionAggregateId, Integer userAggregateId, String name) {
        super(executionAggregateId);
        setUserAggregateId(userAggregateId);
        setName(name);
    }

    public Integer getUserAggregateId() {
        return userAggregateId;
    }

    public void setUserAggregateId(Integer userAggregateId) {
        this.userAggregateId = userAggregateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
