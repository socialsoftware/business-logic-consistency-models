package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Set;

@Embeddable
public class TournamentTopic {
    @Column(name = "topic_aggregate_id")
    private Integer aggregateId;
    @Column(name = "topic_name")
    private String name;
    @Column(name = "topic_course_aggregate_id")
    private Integer courseAggregateId;
    @Column(name = "topic_version")
    private Integer version;
    @Enumerated(EnumType.STRING)
    private Aggregate.AggregateState state;

    public TournamentTopic() {

    }
    public TournamentTopic(TopicDto topicDto) {
        setAggregateId(topicDto.getAggregateId());
        setVersion(topicDto.getVersion());
        setName(topicDto.getName());
        setCourseAggregateId(topicDto.getCourseId());
        setState(Aggregate.AggregateState.ACTIVE);
    }

    public TournamentTopic(TournamentTopic other) {
        setAggregateId(other.getAggregateId());
        setVersion(other.getVersion());
        setName(other.getName());
        setCourseAggregateId(other.getCourseAggregateId());
        setState(other.getState());
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

    public Integer getCourseAggregateId() {
        return courseAggregateId;
    }

    public void setCourseAggregateId(Integer courseId) {
        this.courseAggregateId = courseId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Aggregate.AggregateState getState() {
        return state;
    }

    public void setState(Aggregate.AggregateState state) {
        this.state = state;
    }

    public TopicDto buildDto() {
        TopicDto topicDto = new TopicDto();
        topicDto.setAggregateId(getAggregateId());
        topicDto.setVersion(getVersion());
        topicDto.setName(getName());
        topicDto.setState(getState().toString());
        return topicDto;
    }

    public static void syncTopicVersions(Set<TournamentTopic> prevTopics, Set<TournamentTopic> v1Topics, Set<TournamentTopic> v2Topics) {
        for(TournamentTopic t1 : v1Topics) {
            for(TournamentTopic t2 : v2Topics) {
                if(t1.getAggregateId().equals(t2.getAggregateId())) {
                    if(t1.getVersion() > t2.getVersion()) {
                        t2.setVersion(t1.getVersion());
                        t2.setName(t1.getName());
                    }

                    if(t2.getVersion() > t1.getVersion()) {
                        t1.setVersion(t2.getVersion());
                        t1.setName(t2.getName());
                    }
                }
            }

            // no need to check again because the prev does not contain any newer version than v1 an v2
            for(TournamentTopic tp2 : prevTopics) {
                if(t1.getAggregateId().equals(tp2.getAggregateId())) {
                    if(t1.getVersion() > tp2.getVersion()) {
                        tp2.setVersion(t1.getVersion());
                        tp2.setName(t1.getName());
                    }

                    if(tp2.getVersion() > t1.getVersion()) {
                        t1.setVersion(tp2.getVersion());
                        t1.setName(tp2.getName());
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

        return getAggregateId() != null && getAggregateId().equals(tournamentTopic.getAggregateId()) &&
               getVersion() != null && getVersion().equals(tournamentTopic.getVersion());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getAggregateId();
        hash = 31 * hash + (getVersion() == null ? 0 : getVersion().hashCode());
        return hash;
    }
}
