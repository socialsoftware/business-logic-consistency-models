package pt.ulisboa.tecnico.socialsoftware.blcm.event;

public class RemoveQuestionEvent extends DomainEvent{
    private Integer aggregateId;


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
