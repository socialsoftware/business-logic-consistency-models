package pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork;

import pt.ulisboa.tecnico.socialsoftware.blcm.event.DomainEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;

import java.util.*;


public class UnitOfWork {

    private Integer version;

    private Map<Integer, Aggregate> updatedObjects;

    private Set<DomainEvent> eventsToEmit;

    // Cumulative dependencies of the functionality
    // Map type ensures only a version of an aggregate is written by transaction
    private Map<Integer, Aggregate> currentReadDependencies;

    public UnitOfWork(Integer version) {
        this.updatedObjects = new HashMap<Integer, Aggregate>();
        this.eventsToEmit = new HashSet<>();
        setVersion(version);
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Collection<Aggregate> getUpdatedObjects() {
        return updatedObjects.values();
    }

    public Map<Integer, Aggregate> getUpdatedObjectsMap() {
        return updatedObjects;
    }

    // TODO store type in aggregate
    public void addUpdatedObject(Aggregate aggregate) {
        // the id to null is to enforce a new entry in the db
        aggregate.setId(null);
        this.updatedObjects.put(aggregate.getAggregateId(), aggregate);
    }

    public Set<DomainEvent> getEventsToEmit() {
        return eventsToEmit;
    }

    public void addEvent(DomainEvent event) {
        this.eventsToEmit.add(event);
    }

    public Map<Integer, Aggregate> getCurrentReadDependencies() {
        return currentReadDependencies;
    }

    public void addCurrentReadDependency(Aggregate dep) {
        if(!this.currentReadDependencies.containsKey(dep.getAggregateId())) {
            this.currentReadDependencies.put(dep.getAggregateId(), dep);
        }
    }

    public boolean hasAggregateDep(Integer aggregateId) {
        return this.currentReadDependencies.containsKey(aggregateId);
    }

    public Aggregate getAggregateDep(Integer aggregateId) {
        return this.currentReadDependencies.get(aggregateId);
    }

    /*public void addDependency(Integer objAggregateId, Dependency dep) {
        if(this.updatedObjects.containsKey(objAggregateId)) {
            AggregateIdTypePair pair = this.updatedObjects.get(objAggregateId);
            pair.addDependency(dep);
        }
    }*/


}
