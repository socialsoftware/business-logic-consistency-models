package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.event.publish;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.Event;

@Entity
public class RemoveCourseExecutionEvent extends Event {
    public RemoveCourseExecutionEvent() {}

    public RemoveCourseExecutionEvent(Integer aggregateId) {
        super(aggregateId);
    }
}
