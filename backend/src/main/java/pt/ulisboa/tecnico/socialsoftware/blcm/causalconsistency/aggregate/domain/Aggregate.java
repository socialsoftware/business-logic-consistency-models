package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.DELETED;

//@MappedSuperclass
@Entity
@Embeddable
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Aggregate {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "aggregate_id")
    private Integer aggregateId;

    @Column
    private Integer version;

    @Column(name = "creation_ts")
    private LocalDateTime creationTs;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private AggregateState state;

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregate_type")
    private AggregateType aggregateType;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, Integer> processedEvents;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, Integer> emittedEvents;

    @ManyToOne
    private Aggregate prev;

    public void remove() {
        setState(DELETED);
    }


    public enum AggregateState {
        ACTIVE,
        INACTIVE,
        DELETED
    }

    public Aggregate() {

    }

    /* used when creating a new aggregate*/
    public Aggregate(Integer aggregateId, AggregateType aggregateType) {
        setAggregateId(aggregateId);
        setState(AggregateState.ACTIVE);
        setAggregateType(aggregateType);
        setEmittedEvents(new HashMap<>());
        setProcessedEvents(new HashMap<>());
    }


    public abstract boolean verifyInvariants();

    //public abstract Aggregate merge(Aggregate prev, Aggregate v1, Aggregate v2);

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getCreationTs() {
        return creationTs;
    }

    public void setCreationTs(LocalDateTime creationTs) {
        this.creationTs = creationTs;
    }

    public AggregateState getState() {
        return state;
    }

    public void setState(AggregateState state) {
        this.state = state;
    }

    public AggregateType getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(AggregateType aggregateType) {
        this.aggregateType = aggregateType;
    }

    public Aggregate getPrev() {
        return this.prev;
    }

    public void setPrev(Aggregate prev) {
        this.prev = prev;
    }

    public Map<String, Integer> getProcessedEvents() {
        return processedEvents;
    }

    public void setProcessedEvents(Map<String, Integer> processedEvents) {
        this.processedEvents = processedEvents;
    }

    public void addProcessedEvent(String eventType, Integer eventVersion) {
        if(this.processedEvents.containsKey(eventType) && this.processedEvents.get(eventType) >= eventVersion) {
            return;
        }
        this.processedEvents.put(eventType, eventVersion);
    }

    public Map<String, Integer> getEmittedEvents() {
        return emittedEvents;
    }

    public void setEmittedEvents(Map<String, Integer> emittedEvents) {
        this.emittedEvents = emittedEvents;
    }

    public void addEmittedEvent(String eventType, Integer eventVersion) {
        if(this.emittedEvents.containsKey(eventType) && this.emittedEvents.get(eventType) >= eventVersion) {
            return;
        }
        this.emittedEvents.put(eventType, eventVersion);
    }

    public void setEmittedEventsVersion(Integer commitVersion) {
        for(String eventType : this.emittedEvents.keySet()) {
            this.emittedEvents.put(eventType, commitVersion);
        }
    }

    public abstract Aggregate merge(Aggregate other);

    public abstract Map<Integer, Integer> getSnapshotElements();

    public abstract Set<String> getEventSubscriptions();

}
