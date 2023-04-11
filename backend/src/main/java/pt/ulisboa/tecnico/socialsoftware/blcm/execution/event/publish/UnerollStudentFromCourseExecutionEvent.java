package pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;

@Entity
public class UnerollStudentFromCourseExecutionEvent extends Event {
    private Integer studentAggregateId;

    public UnerollStudentFromCourseExecutionEvent() {
        super();
    }

    public UnerollStudentFromCourseExecutionEvent(Integer courseExecutionAggregateId, Integer studentAggregateId) {
        super(courseExecutionAggregateId);
        setStudentAggregateId(studentAggregateId);
    }

    public Integer getStudentAggregateId() {
        return studentAggregateId;
    }

    public void setStudentAggregateId(Integer studentAggregateId) {
        this.studentAggregateId = studentAggregateId;
    }
}