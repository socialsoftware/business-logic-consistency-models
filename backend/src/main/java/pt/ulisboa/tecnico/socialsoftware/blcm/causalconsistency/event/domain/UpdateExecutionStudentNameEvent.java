package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.Event;

@Entity
public class UpdateExecutionStudentNameEvent extends Event {

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
