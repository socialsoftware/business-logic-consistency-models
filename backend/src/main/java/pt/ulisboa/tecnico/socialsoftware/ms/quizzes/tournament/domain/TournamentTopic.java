package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.dto.TopicDto;

import java.util.Set;

@Entity
public class TournamentTopic {
    @Id
    @GeneratedValue
    private Long id;
    private Integer topicAggregateId;
    private String topicName;
    private Integer topicCourseAggregateId;
    private Integer topicVersion;
    @Enumerated(EnumType.STRING)
    private Aggregate.AggregateState state;
    @ManyToOne
    private Tournament tournament;

    public TournamentTopic() {

    }
    public TournamentTopic(TopicDto topicDto) {
        setTopicAggregateId(topicDto.getAggregateId());
        setTopicVersion(topicDto.getVersion());
        setTopicName(topicDto.getName());
        setTopicCourseAggregateId(topicDto.getCourseId());
        setState(Aggregate.AggregateState.ACTIVE);
    }

    public TournamentTopic(TournamentTopic other) {
        setTopicAggregateId(other.getTopicAggregateId());
        setTopicVersion(other.getTopicVersion());
        setTopicName(other.getTopicName());
        setTopicCourseAggregateId(other.getTopicCourseAggregateId());
        setState(other.getState());
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Integer getTopicAggregateId() {
        return topicAggregateId;
    }

    public void setTopicAggregateId(Integer id) {
        this.topicAggregateId = id;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Integer getTopicCourseAggregateId() {
        return topicCourseAggregateId;
    }

    public void setTopicCourseAggregateId(Integer courseId) {
        this.topicCourseAggregateId = courseId;
    }

    public Integer getTopicVersion() {
        return topicVersion;
    }

    public void setTopicVersion(Integer topicVersion) {
        this.topicVersion = topicVersion;
    }

    public Aggregate.AggregateState getState() {
        return state;
    }

    public void setState(Aggregate.AggregateState state) {
        this.state = state;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public TopicDto buildDto() {
        TopicDto topicDto = new TopicDto();
        topicDto.setAggregateId(getTopicAggregateId());
        topicDto.setVersion(getTopicVersion());
        topicDto.setName(getTopicName());
        topicDto.setState(getState().toString());
        return topicDto;
    }

    public static void syncTopicVersions(Set<TournamentTopic> prevTopics, Set<TournamentTopic> v1Topics, Set<TournamentTopic> v2Topics) {
        for(TournamentTopic t1 : v1Topics) {
            for(TournamentTopic t2 : v2Topics) {
                if(t1.getTopicAggregateId().equals(t2.getTopicAggregateId())) {
                    if(t1.getTopicVersion() > t2.getTopicVersion()) {
                        t2.setTopicVersion(t1.getTopicVersion());
                        t2.setTopicName(t1.getTopicName());
                    }

                    if(t2.getTopicVersion() > t1.getTopicVersion()) {
                        t1.setTopicVersion(t2.getTopicVersion());
                        t1.setTopicName(t2.getTopicName());
                    }
                }
            }

            // no need to check again because the prev does not contain any newer version than v1 an v2
            for(TournamentTopic tp2 : prevTopics) {
                if(t1.getTopicAggregateId().equals(tp2.getTopicAggregateId())) {
                    if(t1.getTopicVersion() > tp2.getTopicVersion()) {
                        tp2.setTopicVersion(t1.getTopicVersion());
                        tp2.setTopicName(t1.getTopicName());
                    }

                    if(tp2.getTopicVersion() > t1.getTopicVersion()) {
                        t1.setTopicVersion(tp2.getTopicVersion());
                        t1.setTopicName(tp2.getTopicName());
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof TournamentTopic)) {
            return false;
        }
        TournamentTopic tournamentTopic = (TournamentTopic) obj;

        return getTopicAggregateId() != null && getTopicAggregateId().equals(tournamentTopic.getTopicAggregateId()) &&
               getTopicVersion() != null && getTopicVersion().equals(tournamentTopic.getTopicVersion());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getTopicAggregateId();
        hash = 31 * hash + (getTopicVersion() == null ? 0 : getTopicVersion().hashCode());
        return hash;
    }
}
