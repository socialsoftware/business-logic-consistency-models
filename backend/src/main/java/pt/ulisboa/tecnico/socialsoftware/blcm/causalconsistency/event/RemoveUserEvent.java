package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("REMOVE_USER")
public class RemoveUserEvent extends Event {

    public RemoveUserEvent() {
        super();
    }

    public RemoveUserEvent(User user) {
        super(user.getAggregateId());
    }
}




