package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.UPDATE_EXECUTION_STUDENT_NAME;

@Entity
@DiscriminatorValue(UPDATE_EXECUTION_STUDENT_NAME)
public class UpdateExecutionStudentNameEvent extends Event{

    private Integer userAggregateId;
    private String name;

    public UpdateExecutionStudentNameEvent() {

    }

    public UpdateExecutionStudentNameEvent(Integer executionAggregateId, Integer userAggregateId, String name) {
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