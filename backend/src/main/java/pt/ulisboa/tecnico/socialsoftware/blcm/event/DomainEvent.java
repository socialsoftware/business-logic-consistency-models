package pt.ulisboa.tecnico.socialsoftware.blcm.event;

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
    private boolean processed;

    public DomainEvent() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
