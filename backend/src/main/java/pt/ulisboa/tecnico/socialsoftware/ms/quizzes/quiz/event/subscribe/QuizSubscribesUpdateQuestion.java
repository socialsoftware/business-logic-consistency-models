package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.event.publish.UpdateQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.domain.QuizQuestion;

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