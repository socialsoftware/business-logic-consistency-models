package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AggregateIdTypePair {
    private Integer objectId;

    private String type;

    private boolean isCommitted;

    private Map<Integer, Dependency> dependencies;

    public AggregateIdTypePair(Integer objectId, String type) {
        this.objectId = objectId;
        this.type = type;
        this.isCommitted = false;
        this.dependencies = new HashMap<>();
    }

    public Integer getObjectId() {
        return objectId;
    }

    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isCommitted() {
        return isCommitted;
    }

    public void setCommitted(boolean committed) {
        isCommitted = committed;
    }

    public Map<Integer, Dependency> getDependencies() {
        return dependencies;
    }

    public void addDependencies(Set<Dependency> dependencies) {
        dependencies.forEach(dep -> {
            this.dependencies.put(dep.getAggregateId(), dep);
        });
    }

    public void addDependency(Dependency dep) {
        this.dependencies.put(dep.getAggregateId(), dep);
    }
}
