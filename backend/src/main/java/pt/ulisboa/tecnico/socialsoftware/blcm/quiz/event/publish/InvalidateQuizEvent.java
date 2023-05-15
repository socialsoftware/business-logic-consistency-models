package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.event.publish;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;

@Entity
public class InvalidateQuizEvent extends Event {

    public InvalidateQuizEvent() {
        super();
    }

    public InvalidateQuizEvent(Integer quizAggregateId) {
        super(quizAggregateId);
    }
}
