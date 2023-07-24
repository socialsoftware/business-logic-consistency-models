package pt.ulisboa.tecnico.socialsoftware.ms.quiz.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.question.event.publish.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quiz.domain.QuizQuestion;

public class QuizSubscribesRemoveQuestion extends EventSubscription {
    public QuizSubscribesRemoveQuestion(QuizQuestion quizQuestion) {
        super(quizQuestion.getQuestionAggregateId(),
                quizQuestion.getQuestionVersion(),
                RemoveQuestionEvent.class.getSimpleName());
    }

    public boolean subscribesEvent(Event event) {
         return super.subscribesEvent(event);
    }

}