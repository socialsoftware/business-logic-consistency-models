package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import java.time.LocalDateTime;
@Embeddable
public class TournamentParticipant {
    @Column(name = "participant_aggregate_id")
    private Integer aggregateId;

    @Column(name = "participant_name")
    private String name;

    @Column(name = "participant_username")
    private String username;

    @Column(name = "participant_enrolltime")
    private LocalDateTime enrollTime;

    @Embedded
    @Column(name = "tournament_answer")
    private TournamentParticipantAnswer answer;

    public TournamentParticipant() {

    }
    public TournamentParticipant(Integer aggregateId, String name, String username) {
        this.aggregateId = aggregateId;
        this.name = name;
        this.username = username;
    }


    public void answerQuiz() {
        this.answer.setAnswered(true);
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer id) {
        this.aggregateId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getEnrollTime() {
        return enrollTime;
    }

    public void setEnrollTime(LocalDateTime enrollTime) {
        this.enrollTime = enrollTime;
    }

    public TournamentParticipantAnswer getAnswer() {
        return answer;
    }

    public void setAnswer(TournamentParticipantAnswer answer) {
        this.answer = answer;
    }
}
