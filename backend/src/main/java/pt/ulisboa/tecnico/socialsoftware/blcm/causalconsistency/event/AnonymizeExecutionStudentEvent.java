package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.ANONYMIZE_EXECUTION_STUDENT;

@Entity
@DiscriminatorValue(ANONYMIZE_EXECUTION_STUDENT)
public class AnonymizeExecutionStudentEvent extends Event {
    private String name;
    private String username;

    private Integer executionAggregateId;

    public AnonymizeExecutionStudentEvent() {
        super();
    }

    public AnonymizeExecutionStudentEvent(Integer aggregateId, String name, String username, Integer executionAggregateId) {
        super(aggregateId);
        setAggregateId(aggregateId);
        setName(name);
        setUsername(username);
        setExecutionAggregateId(executionAggregateId);
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

    public Integer getExecutionAggregateId() {
        return executionAggregateId;
    }

    public void setExecutionAggregateId(Integer executionAggregateId) {
        this.executionAggregateId = executionAggregateId;
    }
}
