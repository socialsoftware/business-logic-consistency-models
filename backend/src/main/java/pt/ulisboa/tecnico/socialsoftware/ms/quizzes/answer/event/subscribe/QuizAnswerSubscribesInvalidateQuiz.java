package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.event.publish.InvalidateQuizEvent;

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