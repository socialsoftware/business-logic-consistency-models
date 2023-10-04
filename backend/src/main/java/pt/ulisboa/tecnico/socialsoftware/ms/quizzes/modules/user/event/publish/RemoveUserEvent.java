package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.user.event.publish;

import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;

import jakarta.persistence.Entity;

@Entity
public class RemoveUserEvent extends Event {
    public RemoveUserEvent() {
        super();
    }

    public RemoveUserEvent(Integer userAggregateId) {
        super(userAggregateId);
    }
}




