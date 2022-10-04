package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("REMOVE_QUESTION")
public class RemoveQuestionEvent extends DomainEvent{
    private Integer aggregateId;

    public RemoveQuestionEvent() {
        super();
    }

    public RemoveQuestionEvent(Integer aggregateId) {
        super();
        setAggregateId(aggregateId);
    }



    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

}
