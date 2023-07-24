package pt.ulisboa.tecnico.socialsoftware.ms.tournament.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.tournament.domain.TournamentCourseExecution;

public class TournamentSubscribesRemoveCourseExecution extends EventSubscription {
    public TournamentSubscribesRemoveCourseExecution(TournamentCourseExecution tournamentCourseExecution) {
        super(tournamentCourseExecution.getCourseExecutionAggregateId(),
                tournamentCourseExecution.getCourseExecutionVersion(),
                RemoveCourseExecutionEvent.class.getSimpleName());
    }

    public boolean subscribesEvent(Event event) {
         return super.subscribesEvent(event);
    }

}