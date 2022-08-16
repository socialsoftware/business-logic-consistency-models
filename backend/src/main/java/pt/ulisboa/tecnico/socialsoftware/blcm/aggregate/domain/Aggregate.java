package pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.DELETED;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dependencies")
    private Map<Integer, Aggregate> dependencies;

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
    public Aggregate(Integer aggregateId, Integer version) {
        setAggregateId(aggregateId);
        setVersion(version);
        setCreationTs(LocalDateTime.now());
        setState(AggregateState.INACTIVE);
    }

    /* used when updating the aggregate */
    public Aggregate(Integer aggregateId) {
        setAggregateId(aggregateId);
    }

    public abstract boolean verifyInvariants();

    //public abstract T merge(T prev, T v1, T v2);

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


    public abstract Aggregate getPrev();

    public Map<Integer, Aggregate> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Map<Integer, Aggregate> dependencies) {
        this.dependencies = dependencies;
    }
}
