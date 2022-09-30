package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type",
        discriminatorType = DiscriminatorType.STRING)
@Table(name = "domain_events")
public abstract class DomainEvent {
    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private Integer  aggregateVersion;


    @Column(name = "type", insertable = false, updatable = false)
    private String type;

    public DomainEvent() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAggregateVersion() {
        return aggregateVersion;
    }

    public void setAggregateVersion(Integer aggregateVersion) {
        this.aggregateVersion = aggregateVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
