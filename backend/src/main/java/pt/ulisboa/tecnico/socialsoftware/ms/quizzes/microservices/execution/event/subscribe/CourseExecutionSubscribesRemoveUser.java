package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.aggregate.CourseExecutionStudent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.user.event.publish.RemoveUserEvent;

public class CourseExecutionSubscribesRemoveUser extends EventSubscription {
    public CourseExecutionSubscribesRemoveUser(CourseExecutionStudent courseExecutionStudent) {
        super(courseExecutionStudent.getUserAggregateId(),
                courseExecutionStudent.getUserVersion(),
                RemoveUserEvent.class.getSimpleName());
    }

    public boolean subscribesEvent(Event event) {
         return super.subscribesEvent(event);
    }

}