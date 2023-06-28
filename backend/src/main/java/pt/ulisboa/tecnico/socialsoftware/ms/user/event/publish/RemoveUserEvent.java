package pt.ulisboa.tecnico.socialsoftware.ms.user.event.publish;

import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.Event;

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




