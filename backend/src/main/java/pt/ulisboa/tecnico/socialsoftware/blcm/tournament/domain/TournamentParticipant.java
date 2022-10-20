package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.CANNOT_UPDATE_TOURNAMENT;

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

    @Column(name = "participant_version")
    private Integer version;

    @Enumerated(EnumType.STRING)
    private AggregateState state;

    public TournamentParticipant() {
        setEnrollTime(LocalDateTime.now());
    }

    public TournamentParticipant(UserDto userDto) {
        setAggregateId(userDto.getAggregateId());
        setName(userDto.getName());
        setUsername(userDto.getUsername());
        setVersion(userDto.getVersion());
        setAnswer(new TournamentParticipantAnswer());
        setEnrollTime(LocalDateTime.now());
        setState(AggregateState.ACTIVE);
    }

    public TournamentParticipant(TournamentParticipant other) {
        setAggregateId(other.getAggregateId());
        setName(other.getName());
        setUsername(other.getUsername());
        setVersion(other.getVersion());
        setAnswer(new TournamentParticipantAnswer(other.getAnswer()));
        setEnrollTime(other.getEnrollTime());
        setState(other.getState());
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
        /*
        AFTER_END
		    now > this.endTime => p: this.participant | final p.answer
		IS_CANCELED
		    this.canceled => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final this.participants && p: this.participant | final p.answer

        */
        // TODO cant access tournament fields such as prev
        /*if(LocalDateTime.now().isAfter(getEndTime())) {
            throw new TutorException(CANNOT_UPDATE_TOURNAMENT, getAggregateId());
        }*/
        this.answer = answer;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public AggregateState getState() {
        return state;
    }

    public void setState(AggregateState state) {
        this.state = state;
    }

    public void updateAnswerWithQuestion(Integer answerAggregateId, boolean isCorrect, Integer eventVersion) {
        getAnswer().setAggregateId(answerAggregateId);
        getAnswer().incrementAnswered();
        if (isCorrect) {
            getAnswer().incrementCorrect();
        }
        getAnswer().setVersion(eventVersion);
    }

    public UserDto buildDto() {
        UserDto userDto = new UserDto();
        userDto.setAggregateId(getAggregateId());
        userDto.setVersion(getVersion());
        userDto.setName(getName());
        userDto.setUsername(getUsername());
        userDto.setNumberAnswered(getAnswer().getNumberOfAnswered());
        userDto.setNumberCorrect(getAnswer().getNumberOfCorrect());
        return userDto;
    }

    public static void syncParticipantVersions(Set<TournamentParticipant> prevParticipants, Set<TournamentParticipant> v1Participants, Set<TournamentParticipant> v2Participants) {
        for(TournamentParticipant tp1 : v1Participants) {
            for(TournamentParticipant tp2 : v2Participants) {
                if(tp1.getAggregateId().equals(tp2.getAggregateId())) {
                    if(tp1.getVersion() > tp2.getVersion()) {
                        tp2.setVersion(tp1.getVersion());
                        tp2.setName(tp1.getName());
                        tp2.setUsername(tp1.getUsername());
                        if(tp1.getAnswer() != null) {
                            tp2.setAnswer(new TournamentParticipantAnswer(tp1.getAnswer()));
                        }
                    }

                    if(tp2.getVersion() > tp1.getVersion()) {
                        tp1.setVersion(tp2.getVersion());
                        tp1.setName(tp2.getName());
                        tp1.setUsername(tp2.getUsername());
                        if(tp2.getAnswer() != null) {
                            tp1.setAnswer(new TournamentParticipantAnswer(tp2.getAnswer()));
                        }
                    }
                }
            }

            // no need to check again because the prev does not contain any newer version than v1 an v2
            for(TournamentParticipant prevParticipant : prevParticipants) {
                if(tp1.getAggregateId().equals(prevParticipant.getAggregateId())) {
                    if(tp1.getVersion() > prevParticipant.getVersion()) {
                        prevParticipant.setVersion(tp1.getVersion());
                        prevParticipant.setName(tp1.getName());
                        prevParticipant.setUsername(tp1.getUsername());
                        if(tp1.getAnswer() != null) {
                            prevParticipant.setAnswer(new TournamentParticipantAnswer(tp1.getAnswer()));
                        }
                    }

                    if(prevParticipant.getVersion() > tp1.getVersion()) {
                        tp1.setVersion(prevParticipant.getVersion());
                        tp1.setName(prevParticipant.getName());
                        tp1.setUsername(prevParticipant.getUsername());
                        if(prevParticipant.getAnswer() != null) {
                            tp1.setAnswer(new TournamentParticipantAnswer(prevParticipant.getAnswer()));
                        }
                    }
                }
            }
        }
        // TODO in the end sync with the creator
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getAggregateId();
        hash = 31 * hash + (getVersion() == null ? 0 : getVersion().hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TournamentParticipant)) {
            return false;
        }
        TournamentParticipant otherParticipant = (TournamentParticipant) obj;
        boolean r = getAggregateId() != null && getAggregateId().equals(otherParticipant.getAggregateId()) &&
                getVersion() != null && getVersion().equals(otherParticipant.getVersion());
        return r;
    }
}
