package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.map.SingletonMap;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.DomainEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEvents;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.CANNOT_PERFORM_CAUSAL_READ;


@Entity
@Table(name = "unit_of_works")
public class UnitOfWork {
    @Id
    @GeneratedValue
    private Integer id;

    private Integer version;

    @ElementCollection
    private Set<Integer> aggregateIds;

    @Transient
    private Map<Integer, Aggregate> aggregateToCommit;

    @Transient
    private Set<DomainEvent> eventsToEmit;

    // Cumulative dependencies of the functionality
    // Map type ensures only a version of an aggregate is written by transaction
    @Transient
    private Map<Integer, Integer> causalSnapshot;

    @Transient
    private Map<String, Integer> eventSnapshot;

    public UnitOfWork() {

    }

    public UnitOfWork(Integer version) {
        this.aggregateToCommit = new HashMap<>();
        this.eventsToEmit = new HashSet<>();
        this.aggregateIds = new HashSet<>();
        this.causalSnapshot = new HashMap<>();
        this.eventSnapshot = new HashMap<>();
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


    public void addAggregateToCommit(Aggregate aggregate) {
        // the id to null is to force a new entry in the db
        aggregate.setId(null);
        this.aggregateIds.add(aggregate.getAggregateId());
        this.aggregateToCommit.put(aggregate.getAggregateId(), aggregate);
    }

    public Set<DomainEvent> getEventsToEmit() {
        return eventsToEmit;
    }

    public void addEvent(DomainEvent event) {
        this.eventsToEmit.add(event);
    }

    public void addToCausalSnapshot(Aggregate aggregate, Set<DomainEvent> allEvents, Set<ProcessedEvents> allProcessedEvents) {

        verifyProcessedEventsByAggregate(aggregate);

        Set<DomainEvent> currentAggregateEmittedEvents = allEvents.stream()
                .filter(e -> e.getAggregateId().equals(aggregate.getAggregateId()))
                .collect(Collectors.toSet());

        Set<String> currentAggregateEmittedEventTypes = currentAggregateEmittedEvents.stream()
                .map(DomainEvent::getType)
                .collect(Collectors.toSet());

        verifyEmittedAggregateEvents(aggregate, currentAggregateEmittedEvents, currentAggregateEmittedEventTypes);
        addAggregateProcessedEventsToSnapshot(aggregate, allProcessedEvents);
        addAggregateEmittedEventsToSnapshot(currentAggregateEmittedEvents, currentAggregateEmittedEventTypes);
    }

    private void verifyEmittedAggregateEvents(Aggregate aggregate, Set<DomainEvent> currentAggregateEmittedEvents, Set<String> currentAggregateEmittedEventTypes) {
        for(String eventType : currentAggregateEmittedEventTypes) {
            DomainEvent lastEmittedEventByType = currentAggregateEmittedEvents.stream()
                    .filter(e -> e.getType().equals(eventType) && e.getAggregateVersion().equals(aggregate.getVersion()))
                    .findFirst()
                    .orElse(null);

            if(lastEmittedEventByType != null && hasEventType(eventType) && !lastEmittedEventByType.getAggregateVersion().equals(getLastEventByType(eventType))) {
                throw new TutorException(CANNOT_PERFORM_CAUSAL_READ, aggregate.getAggregateId(), aggregate.getVersion());
            }
        }
    }

    private void addAggregateEmittedEventsToSnapshot(Set<DomainEvent> currentAggregateEmittedEvents, Set<String> currentAggregateEmittedEventTypes) {
        for(String eventType : currentAggregateEmittedEventTypes) {
            DomainEvent lastEmittedEventByType = currentAggregateEmittedEvents.stream()
                    .filter(e -> e.getType().equals(eventType))
                    .max(Comparator.comparing(DomainEvent::getAggregateVersion))
                    .orElse(null);
            addEventsToSnapshot(lastEmittedEventByType.getType(), lastEmittedEventByType.getAggregateVersion());
        }
    }

    private void addAggregateProcessedEventsToSnapshot(Aggregate aggregate, Set<ProcessedEvents> allProcessedEvents) {
        Set<ProcessedEvents> aggregateProcessedEvents = allProcessedEvents.stream()
                .filter(pe -> aggregate.getEventSubscriptions().contains(pe.getEventType()))
                .filter(pe -> pe.getAggregateId().equals(aggregate.getAggregateId()))
                .collect(Collectors.toSet());

        for(String eventType : aggregate.getEventSubscriptions()) {
           boolean eventTypeExists = false;
           for(ProcessedEvents processedEvents : aggregateProcessedEvents) {
               if(eventType.equals(processedEvents.getEventType())) {
                   eventTypeExists = true;
                   Integer biggestVersion = processedEvents.getProcessedEventsVersions().stream().max(Comparator.comparing(Integer::intValue)).orElse(0);
                   addEventsToSnapshot(eventType, biggestVersion);
               }
           }
           if(!eventTypeExists) {
               addEventsToSnapshot(eventType, 0);
           }
        }
    }

    private void verifyProcessedEventsByAggregate(Aggregate aggregate) {
        this.eventSnapshot.forEach((eventType, eventId) -> {
            if (aggregate.getEventSubscriptions().equals(eventType)) {
                if(hasEventType(eventType) && !getLastEventByType(eventType).equals(eventId)) {
                    throw new TutorException(CANNOT_PERFORM_CAUSAL_READ, aggregate.getAggregateId(), aggregate.getVersion());
                }
            }
        });
    }

    private void verifySnapshotConsistency(Map<Integer, Integer> aggregateSnapshotElements) {
        aggregateSnapshotElements.forEach((aggregateId, version) -> verifySnapshotElementConsistency(aggregateId, version));
    }

    private void verifySnapshotElementConsistency(Integer aggregateId, Integer version) {
        if (hasSnapshotElement(aggregateId) && !getSnapshotElementVersion(aggregateId).equals(version)) {
            throw new TutorException(CANNOT_PERFORM_CAUSAL_READ, aggregateId, version);
        }
    }

    private void addSnapshotElements(Map<Integer, Integer> aggregateSnapshotElements) {
        aggregateSnapshotElements.keySet().forEach(aggregateId -> {
            if(!hasSnapshotElement(aggregateId)) {
                this.causalSnapshot.put(aggregateId, aggregateSnapshotElements.get(aggregateId));
            }
        });
    }

    public boolean hasSnapshotElement(Integer aggregateId) {
        return this.causalSnapshot.containsKey(aggregateId);
    }

    public Integer getSnapshotElementVersion(Integer aggregateId) {
        return this.causalSnapshot.get(aggregateId);
    }

    public Map<String, Integer> getEventSnapshot() {
        return eventSnapshot;
    }

    public void addEventsToSnapshot(String eventType, Integer eventAggregateVersion) {
        this.eventSnapshot.put(eventType, eventAggregateVersion);
    }

    public boolean hasEventType(String eventType) {
        return this.eventSnapshot.containsKey(eventType);
    }

    public Integer getLastEventByType(String eventType) {
        return this.eventSnapshot.get(eventType);
    }

}
