package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.quiz.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.event.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.quiz.aggregate.QuizCourseExecution;

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