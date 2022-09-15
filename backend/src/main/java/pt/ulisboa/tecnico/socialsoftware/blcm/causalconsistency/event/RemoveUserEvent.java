package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("REMOVE_USER")
public class RemoveUserEvent extends DomainEvent {
    private Integer aggregateId;

    public RemoveUserEvent() {

    }

    public RemoveUserEvent(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }



    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }
}
