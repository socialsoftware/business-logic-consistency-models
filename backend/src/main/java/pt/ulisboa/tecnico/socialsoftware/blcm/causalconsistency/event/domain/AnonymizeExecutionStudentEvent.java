package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.Event;

@Entity
public class AnonymizeExecutionStudentEvent extends Event {
    private String name;
    private String username;
    private Integer userAggregateId;

    public AnonymizeExecutionStudentEvent() {
        super();
    }

    public AnonymizeExecutionStudentEvent(Integer aggregateId, String name, String username, Integer userAggregateId) {
        super(aggregateId);
        setAggregateId(aggregateId);
        setName(name);
        setUsername(username);
        setUserAggregateId(userAggregateId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getUserAggregateId() {
        return userAggregateId;
    }

    public void setUserAggregateId(Integer executionAggregateId) {
        this.userAggregateId = executionAggregateId;
    }
}
