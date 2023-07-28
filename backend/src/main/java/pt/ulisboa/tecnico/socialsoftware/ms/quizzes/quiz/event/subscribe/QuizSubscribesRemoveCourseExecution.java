package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.event.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.domain.QuizCourseExecution;

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