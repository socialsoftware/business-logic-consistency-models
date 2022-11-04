package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateComponent;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
public class AnswerUser extends AggregateComponent {
    @Column(name = "user_state")
    private Aggregate.AggregateState state;

    public AnswerUser() {
        super();
    }

    public AnswerUser(UserDto userDto) {
    super(userDto.getAggregateId(), userDto.getVersion());
    setState(Aggregate.AggregateState.valueOf(userDto.getState()));
    }

    public AnswerUser(AnswerUser other) {
        super(other.getAggregateId(), other.getVersion());
        setState(other.getState());
    }

    public Aggregate.AggregateState getState() {
        return state;
    }

    public void setState(Aggregate.AggregateState state) {
        this.state = state;
    }
}
