package pt.ulisboa.tecnico.socialsoftware.blcm.event;

public class RemoveUserEvent extends DomainEvent {
    private Integer aggregateId;

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
