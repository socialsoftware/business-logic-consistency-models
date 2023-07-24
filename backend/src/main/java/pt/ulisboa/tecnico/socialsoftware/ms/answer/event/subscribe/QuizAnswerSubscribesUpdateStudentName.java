package pt.ulisboa.tecnico.socialsoftware.ms.answer.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.UpdateStudentNameEvent;

public class QuizAnswerSubscribesUpdateStudentName extends EventSubscription {
    private Integer studendAggregateId;

    public QuizAnswerSubscribesUpdateStudentName(QuizAnswer quizAnswer) {
        super(quizAnswer.getAnswerCourseExecution().getCourseExecutionAggregateId(),
                quizAnswer.getAnswerCourseExecution().getCourseExecutionVersion(),
                UpdateStudentNameEvent.class.getSimpleName());

        this.studendAggregateId = quizAnswer.getStudent().getStudentAggregateId();
    }

    public boolean subscribesEvent(Event event) {
        return super.subscribesEvent(event) && checkAnswerInfo((UpdateStudentNameEvent)event);
    }

    private boolean checkAnswerInfo(UpdateStudentNameEvent updateStudentNameEvent) {
        return this.studendAggregateId.equals(updateStudentNameEvent.getStudentAggregateId());
    }


}