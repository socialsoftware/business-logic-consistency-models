package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.quiz.event.publish;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;

@Entity
public class InvalidateQuizEvent extends Event {
    public InvalidateQuizEvent() {
        super();
    }

    public InvalidateQuizEvent(Integer quizAggregateId) {
        super(quizAggregateId);
    }
}
