package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.domain.AnswerCourseExecution;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.event.publish.RemoveCourseExecutionEvent;

public class QuizAnswerSubscribesRemoveCourseExecution extends EventSubscription {
    public QuizAnswerSubscribesRemoveCourseExecution(AnswerCourseExecution answerCourseExecution) {
        super(answerCourseExecution.getCourseExecutionAggregateId(),
                answerCourseExecution.getCourseExecutionVersion(),
                RemoveCourseExecutionEvent.class.getSimpleName());
    }

    public boolean subscribesEvent(Event event) {
        return super.subscribesEvent(event);
    }


}