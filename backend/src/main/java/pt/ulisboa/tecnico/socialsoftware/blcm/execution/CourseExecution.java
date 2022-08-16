package pt.ulisboa.tecnico.socialsoftware.blcm.execution;

import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class CourseExecution extends Aggregate {

    @ManyToOne
    private CourseExecution prev;
    @Override
    public boolean verifyInvariants() {
        return false;
    }

    @Override
    public Aggregate getPrev() {
        return null;
    }

    public void setPrev() {

    }
}
