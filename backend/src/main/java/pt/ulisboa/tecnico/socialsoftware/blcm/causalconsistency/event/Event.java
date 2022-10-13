package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type",
        discriminatorType = DiscriminatorType.STRING)
@Table(name = "events")
public abstract class Event {
    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private Integer aggregateId;

    @Column
    private Integer  aggregateVersion;

    private LocalDateTime ts;


    @Column(name = "type", insertable = false, updatable = false)
    private String type;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
