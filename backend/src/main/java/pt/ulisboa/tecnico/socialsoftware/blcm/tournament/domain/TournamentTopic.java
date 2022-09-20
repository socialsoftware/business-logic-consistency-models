package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;

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

    public TournamentTopic() {

    }
    public TournamentTopic(TopicDto topicDto) {
        setAggregateId(topicDto.getAggregateId());
        setVersion(topicDto.getVersion());
        setName(topicDto.getName());
        setCourseAggregateId(topicDto.getCourseId());
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

    public TopicDto buildDto() {
        TopicDto topicDto = new TopicDto();
        topicDto.setAggregateId(getAggregateId());
        topicDto.setVersion(getVersion());
        topicDto.setName(getName());

        return topicDto;
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
        hash = 31 * hash + (version == null ? 0 : version.hashCode());
        return hash;
    }
}
