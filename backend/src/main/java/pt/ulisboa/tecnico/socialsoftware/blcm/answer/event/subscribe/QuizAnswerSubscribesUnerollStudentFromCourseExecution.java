package pt.ulisboa.tecnico.socialsoftware.blcm.answer.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.UnerollStudentFromCourseExecutionEvent;

public class QuizAnswerSubscribesUnerollStudentFromCourseExecution extends EventSubscription {
    private Integer studentAggregateId;

    public QuizAnswerSubscribesUnerollStudentFromCourseExecution(QuizAnswer quizAnswer) {
        super(quizAnswer.getCourseExecution().getCourseExecutionAggregateId(),
                quizAnswer.getCourseExecution().getCourseExecutionVersion(),
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