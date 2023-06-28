package pt.ulisboa.tecnico.socialsoftware.ms.execution.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.domain.CourseExecutionStudent;
import pt.ulisboa.tecnico.socialsoftware.ms.user.event.publish.RemoveUserEvent;

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