package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Event {
    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private Integer aggregateId;

    @Column
    private Integer  aggregateVersion;

    private LocalDateTime ts;

    public Event() {
        setTs(LocalDateTime.now());
    }

    public Event(Integer aggregateId) {
        setAggregateId(aggregateId);
        setTs(LocalDateTime.now());
    }

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

    public Integer getAggregateVersion() {
        return aggregateVersion;
    }

    public void setAggregateVersion(Integer aggregateVersion) {
        this.aggregateVersion = aggregateVersion;
    }

    public LocalDateTime getTs() {
        return ts;
    }

    public void setTs(LocalDateTime ts) {
        this.ts = ts;
    }

}
