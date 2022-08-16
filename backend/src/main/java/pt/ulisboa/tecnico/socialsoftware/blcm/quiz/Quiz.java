package pt.ulisboa.tecnico.socialsoftware.blcm.quiz;

import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quizzes")
public class Quiz extends Aggregate {
    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    public Quiz() {

    }
    public Quiz(Integer numberOfQuestions, Integer aggregateId, Integer version) {
        setNumberOfQuestions(numberOfQuestions);
        setAggregateId(aggregateId);
        setVersion(version);
        setState(AggregateState.INACTIVE);
    }

    public Quiz(Quiz other) {
        super(other.getAggregateId());
        setId(null); /* to force a new database entry when saving to be able to distinguish between versions of the same aggregate*/
        setNumberOfQuestions(other.getNumberOfQuestions());
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

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
