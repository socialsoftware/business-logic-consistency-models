package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.DomainEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;

import java.util.*;

import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.CANNOT_PERFORM_CAUSAL_READ;


public class UnitOfWork {

    private Integer version;

    private Map<Integer, Aggregate> updatedObjects;

    private Set<DomainEvent> eventsToEmit;

    // Cumulative dependencies of the functionality
    // Map type ensures only a version of an aggregate is written by transaction
    // TODO since aggregate ids are unique amongst several aggregate types, perhaps only a pair <Integer, Integer> is enough ( second Integer being the version)
    private Map<Integer, EventualConsistencyDependency> currentReadDependencies;

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


    public void addUpdatedObject(Aggregate aggregate) {
        // the id to null is to force a new entry in the db
        aggregate.setId(null);
        this.updatedObjects.put(aggregate.getAggregateId(), aggregate);
    }

    public Set<DomainEvent> getEventsToEmit() {
        return eventsToEmit;
    }

    public void addEvent(DomainEvent event) {
        this.eventsToEmit.add(event);
    }

    public Map<Integer, EventualConsistencyDependency> getCurrentReadDependencies() {
        return currentReadDependencies;
    }


    public void addToCausalSnapshot(Aggregate aggregate) {
        verifyEventualConsistency(aggregate);
        addEventDependencies(aggregate);
    }

    private void addEventDependencies(Aggregate aggregate) {
        aggregate.getDependenciesMap().values().forEach(dep -> {
            if(!hasAggregateDep(dep.getAggregateId())) {
                this.currentReadDependencies.put(dep.getAggregateId(), dep);
            }
        });
    }

    private void verifyEventualConsistency(Aggregate aggregate) {
        for(EventualConsistencyDependency dep : aggregate.getDependenciesMap().values()) {
            if (hasAggregateDep(dep.getAggregateId()) && !getAggregateDep(dep.getAggregateId()).getVersion().equals(dep.getVersion())) {
                throw new TutorException(CANNOT_PERFORM_CAUSAL_READ, dep.getAggregateId(), dep.getVersion());
            }
        }
    }

    public boolean hasAggregateDep(Integer aggregateId) {
        return this.currentReadDependencies.containsKey(aggregateId);
    }

    public EventualConsistencyDependency getAggregateDep(Integer aggregateId) {
        return this.currentReadDependencies.get(aggregateId);
    }

}
