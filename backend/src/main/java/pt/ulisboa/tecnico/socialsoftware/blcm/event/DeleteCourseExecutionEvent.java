package pt.ulisboa.tecnico.socialsoftware.blcm.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("COURSE_EXECUTION_DELETE")
public class DeleteCourseExecutionEvent extends DomainEvent {
    private Integer courseExecutionAggregateId;

    public DeleteCourseExecutionEvent() {

    }

    public DeleteCourseExecutionEvent(Integer courseExecutionAggregateId) {
        this.courseExecutionAggregateId = courseExecutionAggregateId;
    }



    public Integer getCourseExecutionId() {
        return courseExecutionAggregateId;
    }

    public void setCourseExecutionAggregateId(Integer coureseExecutionId) {
        this.courseExecutionAggregateId = coureseExecutionId;
    }
}
