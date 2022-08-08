package pt.ulisboa.tecnico.socialsoftware.blcm.quiz;

import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quizzes")
public class Quiz implements Aggregate {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    @Column(name = "aggregate_id", unique = true)
    private Integer aggregateId;

    @Column(name = "version")
    private Integer version;

    @Column(name = "creation_ts")
    private LocalDateTime creationTs;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private AggregateState state;

    public Quiz() {

    }
    public Quiz(Integer numberOfQuestions, Integer aggregateId, Integer version) {
        setNumberOfQuestions(numberOfQuestions);
        setAggregateId(aggregateId);
        setVersion(version);
        setState(AggregateState.INACTIVE);
    }



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    @Override
    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public boolean verifyInvariants() {
        return false;
    }

    @Override
    public LocalDateTime getCreationTs() {
        return null;
    }

    public void setCreationTs(LocalDateTime creationTs) {
        this.creationTs = creationTs;
    }

    @Override
    public AggregateState getState() {
        return this.state;
    }

    @Override
    public void setState(AggregateState state) {
        this.state = state;
    }

    @Override
    public Integer getAggregateId() {
        return this.getAggregateId();
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }
}
