package pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork;

import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.AggregateType;

public class Dependency {
    private Integer aggregateId;

    private AggregateType type;

    private Integer version;

    public Dependency(Integer aggregateId, AggregateType type, Integer version) {
        this.aggregateId = aggregateId;
        this.type = type;
        this.version = version;
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public AggregateType getType() {
        return type;
    }

    public void setType(AggregateType type) {
        this.type = type;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
