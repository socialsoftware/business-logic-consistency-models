package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain;

import javax.persistence.*;

@Entity
@Embeddable
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AggregateComponent {
    @Id
    @GeneratedValue
    private Integer id;
    @Column
    private Integer aggregateId;

    @Column
    private Integer version;


    public AggregateComponent() {

    }

    public AggregateComponent(Integer aggregateId, Integer version) {
        setAggregateId(aggregateId);
        setVersion(version);
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

}
