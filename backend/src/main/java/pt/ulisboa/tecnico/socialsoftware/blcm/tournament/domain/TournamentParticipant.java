package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Embeddable
public class TournamentParticipant {
    private Integer participantAggregateId;
    private String participantName;
    private String participantUsername;
    private LocalDateTime enrollTime;
    @Embedded
    private TournamentParticipantQuizAnswer participantAnswer;
    private Integer participantVersion;
    @Enumerated(EnumType.STRING)
    private AggregateState state;

    public TournamentParticipant() {
        setEnrollTime(LocalDateTime.now());
    }

    public TournamentParticipant(UserDto userDto) {
        setParticipantAggregateId(userDto.getAggregateId());
        setParticipantName(userDto.getName());
        setParticipantUsername(userDto.getUsername());
        setParticipantVersion(userDto.getVersion());
        setParticipantAnswer(new TournamentParticipantQuizAnswer());
        setEnrollTime(LocalDateTime.now());
        setState(AggregateState.ACTIVE);
    }

    public TournamentParticipant(TournamentParticipant other) {
        setParticipantAggregateId(other.getParticipantAggregateId());
        setParticipantName(other.getParticipantName());
        setParticipantUsername(other.getParticipantUsername());
        setParticipantVersion(other.getParticipantVersion());
        setParticipantAnswer(new TournamentParticipantQuizAnswer(other.getParticipantAnswer()));
        setEnrollTime(other.getEnrollTime());
        setState(other.getState());
    }


    public void answerQuiz() {
        this.participantAnswer.setAnswered(true);
    }

    public Integer getParticipantAggregateId() {
        return participantAggregateId;
    }

    public void setParticipantAggregateId(Integer id) {
        this.participantAggregateId = id;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getParticipantUsername() {
        return participantUsername;
    }

    public void setParticipantUsername(String participantUsername) {
        this.participantUsername = participantUsername;
    }

    public LocalDateTime getEnrollTime() {
        return enrollTime;
    }

    public void setEnrollTime(LocalDateTime enrollTime) {
        this.enrollTime = enrollTime;
    }

    public TournamentParticipantQuizAnswer getParticipantAnswer() {
        return participantAnswer;
    }

    public void setParticipantAnswer(TournamentParticipantQuizAnswer participantAnswer) {

        this.participantAnswer = participantAnswer;
    }

    public Integer getParticipantVersion() {
        return participantVersion;
    }

    public void setParticipantVersion(Integer participantVersion) {
        this.participantVersion = participantVersion;
    }

    public AggregateState getState() {
        return state;
    }

    public void setState(AggregateState state) {
        this.state = state;
    }

    public void updateAnswerWithQuestion(Integer quizAnswerAggregateId, Integer questionAnswerAggregateId, boolean isCorrect, Integer version) {
        this.participantAnswer.updateAnswerWithQuestion(quizAnswerAggregateId, questionAnswerAggregateId, isCorrect, version);
    }

    public UserDto buildDto() {
        UserDto userDto = new UserDto();
        userDto.setAggregateId(getParticipantAggregateId());
        userDto.setVersion(getParticipantVersion());
        userDto.setName(getParticipantName());
        userDto.setUsername(getParticipantUsername());
        userDto.setAnswerAggregateId(getParticipantAnswer().getQuizAnswerAggregateId());
        userDto.setNumberAnswered(getParticipantAnswer().getNumberOfAnswered());
        userDto.setNumberCorrect(getParticipantAnswer().getNumberOfCorrect());
        return userDto;
    }

    public static void syncParticipantsVersions(Set<TournamentParticipant> prevParticipants,
                                                Set<TournamentParticipant> v1Participants,
                                                Set<TournamentParticipant> v2Participants,
                                                Integer prevCourseExecutionVersion,
                                                Integer v1CourseExecutionVersion,
                                                Integer v2CourseExecutionVersion) {

        for (TournamentParticipant tp1 : v1Participants) {
            for (TournamentParticipant tp2 : v2Participants) {
                if (tp1.getParticipantAggregateId().equals(tp2.getParticipantAggregateId())) {
                    if (v1CourseExecutionVersion > v2CourseExecutionVersion) {
                        tp2.setParticipantVersion(tp1.getParticipantVersion());
                        tp2.setParticipantName(tp1.getParticipantName());
                        tp2.setParticipantUsername(tp1.getParticipantUsername());
                        if (tp1.getParticipantAnswer() != null) {
                            tp2.setParticipantAnswer(new TournamentParticipantQuizAnswer(tp1.getParticipantAnswer()));
                        }
                    }

                    if (v2CourseExecutionVersion > v1CourseExecutionVersion) {
                        tp1.setParticipantVersion(tp2.getParticipantVersion());
                        tp1.setParticipantName(tp2.getParticipantName());
                        tp1.setParticipantUsername(tp2.getParticipantUsername());
                        if (tp2.getParticipantAnswer() != null) {
                            tp1.setParticipantAnswer(new TournamentParticipantQuizAnswer(tp2.getParticipantAnswer()));
                        }
                    }
                }
            }

            // no need to check again because the prev does not contain any newer version than v1 an v2
            for (TournamentParticipant prevParticipant : prevParticipants) {
                if (tp1.getParticipantAggregateId().equals(prevParticipant.getParticipantAggregateId())) {
                    if (v1CourseExecutionVersion > prevCourseExecutionVersion) {
                        prevParticipant.setParticipantVersion(tp1.getParticipantVersion());
                        prevParticipant.setParticipantName(tp1.getParticipantName());
                        prevParticipant.setParticipantUsername(tp1.getParticipantUsername());
                        if (tp1.getParticipantAnswer() != null) {
                            prevParticipant.setParticipantAnswer(new TournamentParticipantQuizAnswer(tp1.getParticipantAnswer()));
                        }
                    }

                    if (prevCourseExecutionVersion > v1CourseExecutionVersion) {
                        tp1.setParticipantVersion(prevParticipant.getParticipantVersion());
                        tp1.setParticipantName(prevParticipant.getParticipantName());
                        tp1.setParticipantUsername(prevParticipant.getParticipantUsername());
                        if (prevParticipant.getParticipantAnswer() != null) {
                            tp1.setParticipantAnswer(new TournamentParticipantQuizAnswer(prevParticipant.getParticipantAnswer()));
                        }
                    }
                }
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getParticipantAggregateId();
        hash = 31 * hash + (getParticipantVersion() == null ? 0 : getParticipantVersion().hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TournamentParticipant)) {
            return false;
        }
        TournamentParticipant otherParticipant = (TournamentParticipant) obj;
        boolean r = getParticipantAggregateId() != null && getParticipantAggregateId().equals(otherParticipant.getParticipantAggregateId()) &&
                getParticipantVersion() != null && getParticipantVersion().equals(otherParticipant.getParticipantVersion());
        return r;
    }
}
