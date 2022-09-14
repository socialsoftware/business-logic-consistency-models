package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class AnswerUser {
    @Column(name = "user_aggregate_id")
    private Integer aggregateId;

    @Column(name = "user_aggregate_version")
    private Integer version;

    public AnswerUser() {

    }

    public AnswerUser(UserDto userDto) {
        setAggregateId(userDto.getAggregateId());
        setVersion(userDto.getVersion());
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
