package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

@Embeddable
public class AnswerUser {
    private Integer userAggregateId;
    private Aggregate.AggregateState userState;

    public AnswerUser() {
        this.userAggregateId = 0;
    }

    public AnswerUser(UserDto userDto) {
        this.userAggregateId = userDto.getAggregateId();
        setUserState(Aggregate.AggregateState.valueOf(userDto.getState()));
    }

    public AnswerUser(AnswerUser other) {
        this.userAggregateId = other.getUserAggregateId();
        setUserState(other.getUserState());
    }

    public Integer getUserAggregateId() {
        return userAggregateId;
    }

    public void setUserAggregateId(Integer userAggregateId) {
        this.userAggregateId = userAggregateId;
    }

    public Aggregate.AggregateState getUserState() {
        return userState;
    }

    public void setUserState(Aggregate.AggregateState userState) {
        this.userState = userState;
    }
}
