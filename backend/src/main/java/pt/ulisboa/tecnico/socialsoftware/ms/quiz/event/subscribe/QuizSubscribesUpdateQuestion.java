package pt.ulisboa.tecnico.socialsoftware.ms.quiz.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.question.event.publish.UpdateQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quiz.domain.QuizQuestion;

public class QuizSubscribesUpdateQuestion extends EventSubscription {
    public QuizSubscribesUpdateQuestion(QuizQuestion quizQuestion) {
        super(quizQuestion.getQuestionAggregateId(),
                quizQuestion.getQuestionVersion(),
                UpdateQuestionEvent.class.getSimpleName());
    }

    public boolean subscribesEvent(Event event) {
         return super.subscribesEvent(event);
    }

}