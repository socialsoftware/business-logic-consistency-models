package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
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

    // during a transaction we only want to have one primary aggregate and so by default all aggregates are secondary
    @Column(columnDefinition = "boolean default false")
    private boolean primaryAggregate;

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

    public abstract Aggregate merge(Aggregate other);

    public abstract Map<Integer, Integer> getSnapshotElements();

    public AggregateType getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(AggregateType aggregateType) {
        this.aggregateType = aggregateType;
    }

    public boolean isPrimaryAggregate() {
        return primaryAggregate;
    }

    public void setPrimaryAggregate(boolean primary) {
        this.primaryAggregate = primary;
    }

    public Aggregate getPrev() {
        return this.prev;
    }

    public void setPrev(Aggregate prev) {
        this.prev = prev;
    }

    public abstract Set<String> getEventSubscriptions();

}
