package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.question.domain.Question;

import javax.persistence.Entity;

@Entity
public class RemoveQuestionEvent extends Event {

    public RemoveQuestionEvent() {
        super();
    }

    public RemoveQuestionEvent(Question question) {
        super(question.getAggregateId());
    }
}
