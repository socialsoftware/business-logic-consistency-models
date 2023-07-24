package pt.ulisboa.tecnico.socialsoftware.ms.answer.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.AnonymizeStudentEvent;

public class QuizAnswerSubscribesAnonymizeStudent extends EventSubscription {
    private Integer studentAggregateId;

    public QuizAnswerSubscribesAnonymizeStudent(QuizAnswer quizAnswer) {
        super(quizAnswer.getAnswerCourseExecution().getCourseExecutionAggregateId(),
                quizAnswer.getAnswerCourseExecution().getCourseExecutionVersion(),
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