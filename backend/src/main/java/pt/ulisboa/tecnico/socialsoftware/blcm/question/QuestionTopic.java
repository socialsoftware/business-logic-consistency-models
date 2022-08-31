package pt.ulisboa.tecnico.socialsoftware.blcm.question;

import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;

import javax.persistence.Embeddable;

@Embeddable
public class QuestionTopic {
    private Integer aggregateId;

    private String name;

    public QuestionTopic (TopicDto topicDto) {
        setAggregateId(topicDto.getAggregateId());
        setName(topicDto.getName());
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

    public TopicDto buildDto() {
        TopicDto dto = new TopicDto();
        dto.setAggregateId(this.aggregateId);
        dto.setName(this.name);
        return dto;
    }
}
