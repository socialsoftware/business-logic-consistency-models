package pt.ulisboa.tecnico.socialsoftware.blcm.question;

import pt.ulisboa.tecnico.socialsoftware.blcm.course.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class QuestionDto implements Serializable {
    private Integer aggregateId;

    private String title;

    private String content;

    private String creationDate;

    private CourseDto course;

    private Set<TopicDto> topicDto;

    private List<OptionDto> optionDto;

   public QuestionDto(Question question) {
       setAggregateId(question.getAggregateId());
       setTitle(question.getTitle());
       setContent(question.getContent());
       setCreationDate(DateHandler.toISOString(question.getCreationDate()));
       setCourse(question.getCourse().buildDto());
       setTopicDto(question.getTopics().stream()
               .map(QuestionTopic::buildDto)
               .collect(Collectors.toSet()));
       setOptionDto(question.getOptions().stream()
               .map(OptionDto::new)
               .collect(Collectors.toList()));
   }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public CourseDto getCourse() {
        return course;
    }

    public void setCourse(CourseDto course) {
        this.course = course;
    }



    public Set<TopicDto> getTopicDto() {
        return topicDto;
    }

    public void setTopicDto(Set<TopicDto> topicDto) {
        this.topicDto = topicDto;
    }

    public List<OptionDto> getOptionDto() {
        return optionDto;
    }

    public void setOptionDto(List<OptionDto> optionDto) {
        this.optionDto = optionDto;
    }
}
