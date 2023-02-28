package pt.ulisboa.tecnico.socialsoftware.blcm.answer.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.AnonymizeStudentEvent;

public class QuizAnswerSubscribesAnonymizeStudent extends EventSubscription {
    private Integer studentAggregateId;

    public QuizAnswerSubscribesAnonymizeStudent(QuizAnswer quizAnswer) {
        super(quizAnswer.getCourseExecution().getCourseExecutionAggregateId(),
                quizAnswer.getCourseExecution().getCourseExecutionVersion(),
                AnonymizeStudentEvent.class.getSimpleName());

        this.studentAggregateId = quizAnswer.getStudent().getStudentAggregateId();
    }

    public boolean subscribesEvent(Event event) {
        return super.subscribesEvent(event) && checkAnswerInfo((AnonymizeStudentEvent)event);
    }

    private boolean checkAnswerInfo(AnonymizeStudentEvent anonymizeStudentEvent) {
        return this.studentAggregateId.equals(anonymizeStudentEvent.getStudentAggregateId());
    }


}