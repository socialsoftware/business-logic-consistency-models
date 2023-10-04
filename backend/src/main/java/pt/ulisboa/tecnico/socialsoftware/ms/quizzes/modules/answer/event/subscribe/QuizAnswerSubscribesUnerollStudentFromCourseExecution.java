package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.answer.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.execution.event.publish.DisenrollStudentFromCourseExecutionEvent;

public class QuizAnswerSubscribesUnerollStudentFromCourseExecution extends EventSubscription {
    private Integer studentAggregateId;

    public QuizAnswerSubscribesUnerollStudentFromCourseExecution(QuizAnswer quizAnswer) {
        super(quizAnswer.getAnswerCourseExecution().getCourseExecutionAggregateId(),
                quizAnswer.getAnswerCourseExecution().getCourseExecutionVersion(),
                DisenrollStudentFromCourseExecutionEvent.class.getSimpleName());

        this.studentAggregateId = quizAnswer.getStudent().getStudentAggregateId();
    }

    public boolean subscribesEvent(Event event) {
        return super.subscribesEvent(event) && checkAnswerInfo((DisenrollStudentFromCourseExecutionEvent)event);
    }

    private boolean checkAnswerInfo(DisenrollStudentFromCourseExecutionEvent disenrollStudentFromCourseExecutionEvent) {
        return this.studentAggregateId.equals(disenrollStudentFromCourseExecutionEvent.getStudentAggregateId());
    }


}