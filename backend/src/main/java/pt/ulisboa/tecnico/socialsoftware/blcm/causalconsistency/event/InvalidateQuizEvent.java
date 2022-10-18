package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.INVALIDATE_QUIZ;

@Entity
@DiscriminatorValue(INVALIDATE_QUIZ)
public class InvalidateQuizEvent extends Event {

    public InvalidateQuizEvent() {
        super();
    }

    public InvalidateQuizEvent(Integer aggregateId) {
        super(aggregateId);
    }
}
