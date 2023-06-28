package pt.ulisboa.tecnico.socialsoftware.ms.quiz.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quiz.domain.QuizCourseExecution;

public class QuizSubscribesRemoveCourseExecution extends EventSubscription {
    public QuizSubscribesRemoveCourseExecution(QuizCourseExecution quizCourseExecution) {
        super(quizCourseExecution.getCourseExecutionAggregateId(),
                quizCourseExecution.getCourseExecutionVersion(),
                RemoveCourseExecutionEvent.class.getSimpleName());
    }

    public boolean subscribesEvent(Event event) {
         return super.subscribesEvent(event);
    }

}