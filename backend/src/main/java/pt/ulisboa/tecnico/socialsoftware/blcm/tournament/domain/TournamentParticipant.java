package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateComponent;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.CANNOT_UPDATE_TOURNAMENT;


@Entity
public class TournamentParticipant extends AggregateComponent {

    @Column(name = "participant_name")
    private String name;

    @Column(name = "participant_username")
    private String username;

    @Column(name = "participant_enrolltime")
    private LocalDateTime enrollTime;

    @Embedded
    @Column(name = "tournament_answer")
    private TournamentParticipantAnswer answer;

    @Enumerated(EnumType.STRING)
    private AggregateState state;

    public TournamentParticipant() {
        super();
        setEnrollTime(LocalDateTime.now());
    }

    public TournamentParticipant(UserDto userDto) {
        super(userDto.getAggregateId(), userDto.getVersion());
        setName(userDto.getName());
        setUsername(userDto.getUsername());
        setAnswer(new TournamentParticipantAnswer());
        setEnrollTime(LocalDateTime.now());
        setState(AggregateState.ACTIVE);
    }

    public TournamentParticipant(TournamentParticipant other) {
        super(other.getAggregateId(), other.getVersion());
        setName(other.getName());
        setUsername(other.getUsername());
        setAnswer(new TournamentParticipantAnswer(other.getAnswer()));
        setEnrollTime(other.getEnrollTime());
        setState(other.getState());
    }


    public void answerQuiz() {
        this.answer.setAnswered(true);
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

    public static void syncParticipantsVersions(Set<TournamentParticipant> prevParticipants,
                                                Set<TournamentParticipant> v1Participants,
                                                Set<TournamentParticipant> v2Participants,
                                                Integer prevCourseExecutionVersion,
                                                Integer v1CourseExecutionVersion,
                                                Integer v2CourseExecutionVersion) {

        for(TournamentParticipant tp1 : v1Participants) {
            for(TournamentParticipant tp2 : v2Participants) {
                if(tp1.getAggregateId().equals(tp2.getAggregateId())) {
                    if(v1CourseExecutionVersion > v2CourseExecutionVersion) {
                        tp2.setVersion(tp1.getVersion());
                        tp2.setName(tp1.getName());
                        tp2.setUsername(tp1.getUsername());
                        if(tp1.getAnswer() != null) {
                            tp2.setAnswer(new TournamentParticipantAnswer(tp1.getAnswer()));
                        }
                    }

                    if(v2CourseExecutionVersion > v1CourseExecutionVersion) {
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
                    if(v1CourseExecutionVersion > prevCourseExecutionVersion) {
                        prevParticipant.setVersion(tp1.getVersion());
                        prevParticipant.setName(tp1.getName());
                        prevParticipant.setUsername(tp1.getUsername());
                        if(tp1.getAnswer() != null) {
                            prevParticipant.setAnswer(new TournamentParticipantAnswer(tp1.getAnswer()));
                        }
                    }

                    if(prevCourseExecutionVersion > v1CourseExecutionVersion) {
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
