package pt.ulisboa.tecnico.socialsoftware.blcm.question.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class QuestionTopic {
    @Column(name = "topic_aggregate_id")
    private Integer topicAggregateId;

    @Column(name = "topic_name")
    private String name;
    @Column(name = "topic_version")
    private Integer version;

    public QuestionTopic (TopicDto topicDto) {
        setTopicAggregateId(topicDto.getAggregateId());
        setName(topicDto.getName());
        setVersion(topicDto.getVersion());
    }

    public QuestionTopic() {

    }

    public Integer getTopicAggregateId() {
        return topicAggregateId;
    }

    public void setTopicAggregateId(Integer aggregateId) {
        this.topicAggregateId = aggregateId;
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
        dto.setAggregateId(this.topicAggregateId);
        dto.setName(this.name);
        return dto;
    }
}
