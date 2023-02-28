package pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;

@Entity
public class UpdateStudentNameEvent extends Event {
    private Integer studentAggregateId;
    private String updatedName;

    public UpdateStudentNameEvent() {
    }

    public UpdateStudentNameEvent(Integer executionAggregateId, Integer studentAggregateId, String updatedName) {
        super(executionAggregateId);
        setStudentAggregateId(studentAggregateId);
        setUpdatedName(updatedName);
    }

    public Integer getStudentAggregateId() {
        return studentAggregateId;
    }

    public void setStudentAggregateId(Integer studentAggregateId) {
        this.studentAggregateId = studentAggregateId;
    }

    public String getUpdatedName() {
        return updatedName;
    }

    public void setUpdatedName(String updatedName) {
        this.updatedName = updatedName;
    }
}
