package pt.ulisboa.tecnico.socialsoftware.blcm.user.event.publish;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;

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




