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
    // TODO since aggregate ids are unique amongst several aggregate types, perhaps only a pair <Integer, Integer> is enough ( second Integer being the version)
    private Map<Integer, Dependency> currentReadDependencies;

    public UnitOfWork(Integer version) {
        this.updatedObjects = new HashMap<Integer, Aggregate>();
        this.eventsToEmit = new HashSet<>();
        this.currentReadDependencies = new HashMap<>();
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

    public Map<Integer, Dependency> getCurrentReadDependencies() {
        return currentReadDependencies;
    }


    public void addCurrentReadDependencies(Map<Integer, Dependency> deps) {
        deps.values().forEach(dep -> {
            if(!this.currentReadDependencies.containsKey(dep.getAggregateId())) {
                this.currentReadDependencies.put(dep.getAggregateId(), dep);
            }
        });
    }

    public boolean hasAggregateDep(Integer aggregateId) {
        return this.currentReadDependencies.containsKey(aggregateId);
    }

    public Dependency getAggregateDep(Integer aggregateId) {
        return this.currentReadDependencies.get(aggregateId);
    }

}
