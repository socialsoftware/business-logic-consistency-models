package pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/* unique ID generator for each created version */
@Entity
public class AggregateIdGenerator {
    @Id
    @GeneratedValue
    private Integer id;


    public AggregateIdGenerator() {

    }

    public Integer getId() {
        return id;
    }
}
