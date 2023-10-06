package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.user.event.publish;

import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.Event;

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




