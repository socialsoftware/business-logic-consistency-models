package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.dto.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;

import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.CANNOT_PERFORM_CAUSAL_READ;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.CANNOT_PERFORM_CAUSAL_READ_DUE_TO_EMITTED_EVENT_NOT_PROCESSED;


public class UnitOfWork {

    private Integer id;

    private Integer version;

    private Map<Integer, Aggregate> aggregatesToCommit;


    private Set<Event> eventsToEmit;

    private Map<Integer, Aggregate> causalSnapshot;

    public UnitOfWork() {

    }

    public UnitOfWork(Integer version) {
        this.aggregatesToCommit = new HashMap<>();
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

    public Map<Integer, Aggregate> getAggregatesToCommit() {
        return aggregatesToCommit;
    }


    public void registerChanged(Aggregate aggregate) {
        // the id set to null to force a new entry in the db
        aggregate.setId(null);
        this.aggregatesToCommit.put(aggregate.getAggregateId(), aggregate);
    }

    public Set<Event> getEventsToEmit() {
        return eventsToEmit;
    }

    public void addEvent(Event event) {
        this.eventsToEmit.add(event);
    }

    public void addToCausalSnapshot(Aggregate aggregate, List<Event> allEvents) {
        verifyProcessedEventsByAggregate(aggregate, allEvents);
        verifyEmittedEventsByAggregate(aggregate, allEvents);
        verifySameProcessedEvents(aggregate, allEvents);
        addAggregateToSnapshot(aggregate);
    }

    private void verifyProcessedEventsByAggregate(Aggregate aggregate, List<Event> allEvents) {
        for (EventSubscription es : aggregate.getEventSubscriptions()) {
            for (Aggregate snapshotAggregate : this.causalSnapshot.values()) {
                List<Event> snapshotAggregateEmittedEvents = allEvents.stream()
                        .filter(e -> e.getAggregateId().equals(snapshotAggregate.getAggregateId()))
                        .filter(e -> e.getClass().getSimpleName().equals(es.getEventType()))
                        .filter(e -> e.getAggregateVersion() <= snapshotAggregate.getVersion())
                        .filter(e -> e.getAggregateVersion() > es.getSenderLastVersion())
                        .collect(Collectors.toList());
                // snapshotAggregateEmittedEvents is a list of emitted events of the same type of the current sub emitted
                // by the current snapshot aggregate emitted after the version of the current subscription

                // if there are events in those situations we verify whether they are relevant or not for the subscription
                for (Event snapshotAggregateEmittedEvent : snapshotAggregateEmittedEvents) {
                    if (es.subscribesEvent(snapshotAggregateEmittedEvent)) {
                        throw new TutorException(CANNOT_PERFORM_CAUSAL_READ_DUE_TO_EMITTED_EVENT_NOT_PROCESSED, aggregate.getClass().getSimpleName(), snapshotAggregateEmittedEvent.getClass().getSimpleName());
                    }
                }
            }
        }
    }

    private void verifyEmittedEventsByAggregate(Aggregate aggregate, List<Event> allEvents) {
        for (Aggregate snapshotAggregate : this.causalSnapshot.values()) {
            for (EventSubscription es : snapshotAggregate.getEventSubscriptions()) {
                List<Event> aggregateEmittedEvents = allEvents.stream()
                        .filter(e -> e.getAggregateId().equals(snapshotAggregate.getAggregateId()))
                        .filter(e -> e.getClass().getSimpleName().equals(es.getEventType()))
                        .filter(e -> e.getAggregateVersion() <= snapshotAggregate.getVersion())
                        .filter(e -> e.getAggregateVersion() > es.getSenderLastVersion())
                        .collect(Collectors.toList());
                for (Event snapshotAggregateEmittedEvent : aggregateEmittedEvents) {
                    if (es.subscribesEvent(snapshotAggregateEmittedEvent)) {
                        throw new TutorException(CANNOT_PERFORM_CAUSAL_READ, aggregate.getAggregateId(), getVersion());
                    }
                }
            }
        }
    }

    private void verifySameProcessedEvents(Aggregate aggregate, List<Event> allEvents) {
        Set<EventSubscription> aggregateEventSubscriptions = aggregate.getEventSubscriptions();
        for(Aggregate snapshotAggregate : this.causalSnapshot.values()) {
            for(EventSubscription es1 : aggregateEventSubscriptions) {
                for (EventSubscription es2 : snapshotAggregate.getEventSubscriptions()) {
                    // if they correspond to the same aggregate and type
                    if (es1.getSenderAggregateId().equals(es2.getSenderAggregateId()) && es1.getEventType().equals(es2.getEventType())) {
                        Integer minVersion = Math.min(es1.getSenderLastVersion(), es2.getSenderLastVersion());
                        Integer maxVersion = Math.max(es1.getSenderLastVersion(), es2.getSenderLastVersion());
                        List<Event> eventsBetweenAggregates = allEvents.stream()
                                .filter(event -> event.getAggregateId().equals(es1.getSenderAggregateId()))
                                .filter(event -> event.getClass().getSimpleName().equals(es1.getEventType()))
                                .filter(event -> minVersion < event.getAggregateVersion() && event.getAggregateVersion() <= maxVersion)
                                .collect(Collectors.toList());
                        for (Event eventBetweenAggregates : eventsBetweenAggregates) {
                            if(es1.subscribesEvent(eventBetweenAggregates) && es2.subscribesEvent(eventBetweenAggregates)) {
                                throw new TutorException(CANNOT_PERFORM_CAUSAL_READ, aggregate.getAggregateId(), getVersion());
                            }
                        }
                    }
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
