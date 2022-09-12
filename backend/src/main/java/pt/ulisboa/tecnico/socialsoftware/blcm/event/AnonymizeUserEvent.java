package pt.ulisboa.tecnico.socialsoftware.blcm.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static pt.ulisboa.tecnico.socialsoftware.blcm.event.EventType.ANONYMIZE_USER;

@Entity
@DiscriminatorValue(ANONYMIZE_USER)
public class AnonymizeUserEvent extends DomainEvent{
    private Integer userAggregateId;

    public AnonymizeUserEvent() {

    }

    public AnonymizeUserEvent(Integer userAggregateId) {
        this.userAggregateId = userAggregateId;
    }

    public Integer getUserAggregateId() {
        return userAggregateId;
    }

    public void setUserAggregateId(Integer userAggregateId) {
        this.userAggregateId = userAggregateId;
    }
}
