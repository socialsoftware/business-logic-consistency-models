package pt.ulisboa.tecnico.socialsoftware.blcm.question.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentTopic;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class QuestionTopic {
    @Column(name = "topic_aggregate_id")
    private Integer aggregateId;

    @Column(name = "topic_name")
    private String name;
    @Column(name = "topic_version")
    private Integer version;

    public QuestionTopic (TopicDto topicDto) {
        setAggregateId(topicDto.getAggregateId());
        setName(topicDto.getName());
        setVersion(topicDto.getVersion());
    }

    public QuestionTopic() {

    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public TopicDto buildDto() {
        TopicDto dto = new TopicDto();
        dto.setAggregateId(this.aggregateId);
        dto.setName(this.name);
        dto.setVersion(this.version);
        return dto;
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
        if(!(obj instanceof QuestionTopic)) {
            return false;
        }
        QuestionTopic tournamentTopic = (QuestionTopic) obj;

        return getAggregateId() != null && getAggregateId().equals(tournamentTopic.getAggregateId()) &&
                getVersion() != null && getVersion().equals(tournamentTopic.getVersion());
    }
}
