package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;

import jakarta.persistence.Entity;

@Entity
public class RemoveUserEvent extends Event {

    public RemoveUserEvent() {
        super();
    }

    public RemoveUserEvent(User user) {
        super(user.getAggregateId());
    }
}




