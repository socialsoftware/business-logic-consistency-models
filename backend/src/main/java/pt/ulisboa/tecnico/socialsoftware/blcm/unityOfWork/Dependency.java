package pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork;

public class Dependency {
    private Integer aggregateId;

    private String type;

    private Integer version;

    public Dependency(Integer aggregateId, String type, Integer version) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
