package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.execution.event.publish;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;

@Entity
public class RemoveCourseExecutionEvent extends Event {
    public RemoveCourseExecutionEvent() {}

    public RemoveCourseExecutionEvent(Integer aggregateId) {
        super(aggregateId);
    }
}
