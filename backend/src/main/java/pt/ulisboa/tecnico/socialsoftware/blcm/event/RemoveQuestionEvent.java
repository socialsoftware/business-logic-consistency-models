package pt.ulisboa.tecnico.socialsoftware.blcm.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("REMOVE_QUESTION")
public class RemoveQuestionEvent extends DomainEvent{
    private Integer aggregateId;


    public RemoveQuestionEvent() {

    }

    public RemoveQuestionEvent(Integer aggregateId) {
        this.aggregateId = aggregateId;

    }



    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }
}
