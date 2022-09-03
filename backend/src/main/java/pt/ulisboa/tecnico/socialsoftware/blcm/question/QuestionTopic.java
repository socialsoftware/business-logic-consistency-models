package pt.ulisboa.tecnico.socialsoftware.blcm.question;

import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class QuestionTopic {
    @Column(name = "question_aggregate_id")
    private Integer questionAggregateId;

    private String name;

    public QuestionTopic (TopicDto topicDto) {
        setQuestionAggregateId(topicDto.getAggregateId());
        setName(topicDto.getName());
    }

    public QuestionTopic() {

    }

    public Integer getQuestionAggregateId() {
        return questionAggregateId;
    }

    public void setQuestionAggregateId(Integer aggregateId) {
        this.questionAggregateId = aggregateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TopicDto buildDto() {
        TopicDto dto = new TopicDto();
        dto.setAggregateId(this.questionAggregateId);
        dto.setName(this.name);
        return dto;
    }
}
