package pt.ulisboa.tecnico.socialsoftware.ms.answer.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.UnerollStudentFromCourseExecutionEvent;

public class QuizAnswerSubscribesUnerollStudentFromCourseExecution extends EventSubscription {
    private Integer studentAggregateId;

    public QuizAnswerSubscribesUnerollStudentFromCourseExecution(QuizAnswer quizAnswer) {
        super(quizAnswer.getAnswerCourseExecution().getCourseExecutionAggregateId(),
                quizAnswer.getAnswerCourseExecution().getCourseExecutionVersion(),
                UnerollStudentFromCourseExecutionEvent.class.getSimpleName());

        this.studentAggregateId = quizAnswer.getStudent().getStudentAggregateId();
    }

    public boolean subscribesEvent(Event event) {
        return super.subscribesEvent(event) && checkAnswerInfo((UnerollStudentFromCourseExecutionEvent)event);
    }

    private boolean checkAnswerInfo(UnerollStudentFromCourseExecutionEvent unerollStudentFromCourseExecutionEvent) {
        return this.studentAggregateId.equals(unerollStudentFromCourseExecutionEvent.getStudentAggregateId());
    }


}