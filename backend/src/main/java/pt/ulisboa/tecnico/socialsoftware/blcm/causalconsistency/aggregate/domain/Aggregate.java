package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

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


    public abstract void verifyInvariants();

    public abstract Set<String> getEventSubscriptions();

    public abstract Set<String> getFieldsAbleToChange();

    public abstract Set<String> getIntentionFields();

    public abstract Aggregate mergeFields(Set<String> toCommitVersionChangedFields, Aggregate committedVersion, Set<String> committedVersionChangedFields);

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

    public Aggregate merge(Aggregate other) {
        Aggregate prev = getPrev();
        Aggregate toCommitVersion = this;
        Aggregate committedVersion = other;

        if(prev.getClass() != toCommitVersion.getClass() || prev.getClass() != committedVersion.getClass() || toCommitVersion.getClass() != committedVersion.getClass()) {
            throw new TutorException(AGGREGATE_MERGE_FAILURE, getAggregateId());
        }

        if(toCommitVersion.getState() == DELETED) {
            throw new TutorException(AGGREGATE_DELETED, toCommitVersion.getAggregateId());
        }
        /* take the state into account because we don't want to override a deleted object*/

        if(committedVersion.getState() == DELETED) {
            throw new TutorException(AGGREGATE_DELETED, committedVersion.getAggregateId());
        }

        Set<String> toCommitVersionChangedFields = getChangedFields(prev, toCommitVersion);
        Set<String> committedVersionChangedFields = getChangedFields(prev, committedVersion);

        checkIntentions(toCommitVersionChangedFields, committedVersionChangedFields);

        Aggregate mergedAggregate = mergeFields(toCommitVersionChangedFields, committedVersion, committedVersionChangedFields);

        // TODO see explanation for prev assignment in Quiz
        mergedAggregate.setPrev(getPrev());
        return mergedAggregate;
    }

    private Set<String> getChangedFields(Object prevObj, Object obj) {
        Set<String> changedFields = new HashSet<>();
        if(prevObj.getClass() != obj.getClass()) {
            throw new TutorException(AGGREGATE_MERGE_FAILURE, getAggregateId());
        }

        try {
            for(String fieldName : getFieldsAbleToChange()) {

                Field field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);

                Object currentFieldValue = field.get(obj);
                Object prevFieldValue = field.get(prevObj);


                if(currentFieldValue != null && prevFieldValue != null && !(currentFieldValue.equals(prevFieldValue))) {
                    changedFields.add(fieldName);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new TutorException(AGGREGATE_MERGE_FAILURE, getAggregateId());
        }
        return changedFields;
    }

    private void checkIntentions(Set<String> changedFields1, Set<String> changedFields2) {
        for(String f1 : changedFields1) {
            for(String f2 : changedFields2) {
                if(getIntentionFields().contains(f1) && getIntentionFields().contains(f2) && !f1.equals(f2)) {
                    throw new TutorException(AGGREGATE_MERGE_FAILURE, getAggregateId());
                }
            }
        }
    }


}
