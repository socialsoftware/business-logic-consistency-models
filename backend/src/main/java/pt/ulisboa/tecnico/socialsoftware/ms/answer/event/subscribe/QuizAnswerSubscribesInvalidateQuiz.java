package pt.ulisboa.tecnico.socialsoftware.ms.answer.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.quiz.event.publish.InvalidateQuizEvent;

public class QuizAnswerSubscribesInvalidateQuiz extends EventSubscription {
    public QuizAnswerSubscribesInvalidateQuiz(QuizAnswer quizAnswer) {
        super(quizAnswer.getQuiz().getQuizAggregateId(),
                quizAnswer.getQuiz().getQuizVersion(),
                InvalidateQuizEvent.class.getSimpleName());
    }

    public boolean subscribesEvent(Event event) {
        return super.subscribesEvent(event);
    }

}