package pt.ulisboa.tecnico.socialsoftware.blcm.question;

import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class QuestionTopic {
    @Column(name = "topic_aggregate_id")
    private Integer aggregateId;

    private String name;

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
        return dto;
    }
}
