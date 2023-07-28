package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.domain.CourseExecutionStudent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.event.publish.RemoveUserEvent;

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