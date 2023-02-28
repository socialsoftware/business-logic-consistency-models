package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

@Embeddable
public class AnswerStudent {
    private Integer studentAggregateId;
    private String name;
    private Aggregate.AggregateState studentState;

    public AnswerStudent() {
        this.studentAggregateId = 0;
    }

    public AnswerStudent(UserDto userDto) {
        this.studentAggregateId = userDto.getAggregateId();
        this.name = userDto.getName();
        setStudentState(Aggregate.AggregateState.valueOf(userDto.getState()));
    }

    public AnswerStudent(AnswerStudent other) {
        this.studentAggregateId = other.getStudentAggregateId();
        this.name = other.getName();
        setStudentState(other.getStudentState());
    }

    public Integer getStudentAggregateId() {
        return studentAggregateId;
    }

    public void setStudentAggregateId(Integer studentAggregateId) {
        this.studentAggregateId = studentAggregateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Aggregate.AggregateState getStudentState() {
        return studentState;
    }

    public void setStudentState(Aggregate.AggregateState studentState) {
        this.studentState = studentState;
    }
}
