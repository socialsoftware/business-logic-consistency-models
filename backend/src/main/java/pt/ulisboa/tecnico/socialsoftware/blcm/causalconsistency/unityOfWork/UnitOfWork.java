package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork;

import org.apache.commons.collections4.map.SingletonMap;
import org.springframework.data.util.Pair;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.DomainEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;

import javax.persistence.*;
import java.util.*;

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
    // TODO since aggregate ids are unique amongst several aggregate types, perhaps only a pair <Integer, Integer> is enough ( second Integer being the version)

    @Transient
    private Map<Integer, Integer> causalSnapshot;

    public UnitOfWork() {

    }

    public UnitOfWork(Integer version) {
        this.aggregateToCommit = new HashMap<>();
        this.eventsToEmit = new HashSet<>();
        this.aggregateIds = new HashSet<>();
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

    public void addToCausalSnapshot(Aggregate aggregate) {
        verifySnapshotConsistency(aggregate.getSnapshotElements());
        verifySnapshotElementConsistency(aggregate.getAggregateId(), aggregate.getVersion());
        addSnapshotElements(aggregate.getSnapshotElements());
        addSnapshotElements(new SingletonMap<>(aggregate.getAggregateId(), aggregate.getVersion()));
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

}
