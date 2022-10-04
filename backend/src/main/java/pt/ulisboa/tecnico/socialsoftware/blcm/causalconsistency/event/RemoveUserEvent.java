package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("REMOVE_USER")
public class RemoveUserEvent extends DomainEvent {
    private Integer userAggregateId;

    public RemoveUserEvent() {
        super();
    }

    public RemoveUserEvent(Integer userAggregateId) {
        super();
        this.userAggregateId = userAggregateId;
    }



    public Integer getUserAggregateId() {
        return userAggregateId;
    }

    public void setUserAggregateId(Integer aggregateId) {
        this.userAggregateId = aggregateId;
    }
}
