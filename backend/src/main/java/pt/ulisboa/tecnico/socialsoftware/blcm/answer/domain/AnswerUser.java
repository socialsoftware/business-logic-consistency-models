package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class AnswerUser {
    @Column(name = "user_aggregate_id")
    private final Integer aggregateId;

    @Column(name = "user_state")
    private Aggregate.AggregateState state;

    public AnswerUser() {
        this.aggregateId = 0;
    }

    public AnswerUser(UserDto userDto) {
        this.aggregateId = userDto.getAggregateId();
        setState(Aggregate.AggregateState.valueOf(userDto.getState()));
    }

    public AnswerUser(AnswerUser other) {
        this.aggregateId = other.getAggregateId();
        setState(other.getState());
    }

    public Integer getAggregateId() {
        return aggregateId;
    }


    public Aggregate.AggregateState getState() {
        return state;
    }

    public void setState(Aggregate.AggregateState state) {
        this.state = state;
    }
}
