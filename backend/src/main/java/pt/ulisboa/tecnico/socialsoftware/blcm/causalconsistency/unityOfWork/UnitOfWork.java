package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEvents;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;

import java.util.*;

import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.CANNOT_PERFORM_CAUSAL_READ;



public class UnitOfWork {

    private Integer id;

    private Integer version;

    private Map<Integer, Aggregate> aggregateToCommit;

    private Set<Event> eventsToEmit;

    private Map<Integer, Aggregate> causalSnapshot;

    public UnitOfWork() {

    }

    public UnitOfWork(Integer version) {
        this.aggregateToCommit = new HashMap<>();
        this.eventsToEmit = new HashSet<>();
        this.causalSnapshot = new HashMap<>();
        setVersion(version);
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Collection<Aggregate> getAggregateToCommit() {
        return aggregateToCommit.values();
    }

    public Map<Integer, Aggregate> getAggregatesToCommit() {
        return aggregateToCommit;
    }


    public void registerChanged(Aggregate aggregate) {
        // the id to null is to force a new entry in the db
        aggregate.setId(null);
        this.aggregateToCommit.put(aggregate.getAggregateId(), aggregate);
    }

    public Set<Event> getEventsToEmit() {
        return eventsToEmit;
    }

    public void addEvent(Event event) {
        this.eventsToEmit.add(event);
    }

    public void addToCausalSnapshot(Aggregate aggregate) {
        verifyProcessedEventsByAggregate(aggregate);
        verifyEmittedEventsByAggregate(aggregate);
        addAggregateToSnapshot(aggregate);
    }

    private void verifyProcessedEventsByAggregate(Aggregate aggregate) {
        for(String subType : aggregate.getEventSubscriptions()) {
            Integer lastProcessedEventVersion = aggregate.getProcessedEvents().get(subType) != null ? aggregate.getProcessedEvents().get(subType) : 0;
            for(Aggregate snapShotAggregate : this.causalSnapshot.values()) {
                if(snapShotAggregate.getEmittedEvents().containsKey(subType) && !snapShotAggregate.getEmittedEvents().get(subType).equals(lastProcessedEventVersion)) {
                    throw new TutorException(CANNOT_PERFORM_CAUSAL_READ, aggregate.getAggregateId(), getVersion());
                }
            }
        }
    }

    private void verifyEmittedEventsByAggregate(Aggregate aggregate) {
        for(Aggregate snapShotAggregate : this.causalSnapshot.values()) {
            for(String subType : snapShotAggregate.getEventSubscriptions()) {
                Integer lastProcessedEventVersion = snapShotAggregate.getProcessedEvents().get(subType) != null ? snapShotAggregate.getProcessedEvents().get(subType) : 0;
                if(aggregate.getProcessedEvents().containsKey(subType) && !aggregate.getEmittedEvents().get(subType).equals(lastProcessedEventVersion)) {
                    throw new TutorException(CANNOT_PERFORM_CAUSAL_READ, aggregate.getAggregateId(), getVersion());
                }
            }
        }
    }

    private void addAggregateToSnapshot(Aggregate aggregate) {
        if(!this.causalSnapshot.containsKey(aggregate.getAggregateId()) || !(aggregate.getVersion() <= this.causalSnapshot.get(aggregate.getAggregateId()).getVersion())) {
            this.causalSnapshot.put(aggregate.getAggregateId(), aggregate);
        }
    }
}
