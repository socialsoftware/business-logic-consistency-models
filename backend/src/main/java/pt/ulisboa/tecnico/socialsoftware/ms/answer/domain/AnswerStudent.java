package pt.ulisboa.tecnico.socialsoftware.ms.answer.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.user.dto.UserDto;

@Entity
public class AnswerStudent {
    @Id
    @GeneratedValue
    private Long id;
    private Integer studentAggregateId;
    private String name;
    private Aggregate.AggregateState studentState;
    @OneToOne
    private QuizAnswer quizAnswer;

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

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
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

    public QuizAnswer getQuizAnswer() {
        return quizAnswer;
    }

    public void setQuizAnswer(QuizAnswer quizAnswer) {
        this.quizAnswer = quizAnswer;
    }
}
