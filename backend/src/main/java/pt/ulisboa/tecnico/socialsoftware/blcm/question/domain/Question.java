package pt.ulisboa.tecnico.socialsoftware.blcm.question.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.QUESTION;

/*
    INTRA-INVARIANTS:

    INTER-INVARIANTS:
        TOPICS_EXIST
        COURSE_EXISTS (course does not send events)
        COURSE_SAME_TOPIC_COURSE ()
 */
@Entity
@Table(name = "questions")
public class Question extends Aggregate {

    @Column
    private String title;

    @Column
    private String content;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Embedded
    private QuestionCourse course;

    @ElementCollection
    private Set<QuestionTopic> topics;

    @ElementCollection
    private List<Option> options;

    public Question() {

    }

    public Question(Integer aggregateId, QuestionCourse course, QuestionDto questionDto, List<QuestionTopic> questionTopics) {
        super(aggregateId, QUESTION);
        setTitle(questionDto.getTitle());
        setContent(questionDto.getContent());
        setCreationDate(LocalDateTime.now());
        setCourse(course);
        setOptions(questionDto.getOptionDtos().stream().map(Option::new).collect(Collectors.toList()));

        Integer optionKeyGenerator = 1;
        for(Option o : getOptions()) {
            o.setKey(optionKeyGenerator++);
        }

        setTopics(new HashSet<>(questionTopics));
        setPrev(null);
    }

    public Question(Question other) {
        super(other.getAggregateId(), QUESTION);
        setId(null);
        setTitle(other.getTitle());
        setContent(other.getContent());
        setCreationDate(other.getCreationDate());
        setCourse(other.getCourse());
        setOptions(other.getOptions());
        setTopics(new HashSet<>(other.getTopics()));
        setProcessedEvents(new HashMap<>(other.getProcessedEvents()));
        setEmittedEvents(new HashMap<>(other.getEmittedEvents()));
        setPrev(other);
    }


    @Override
    public void verifyInvariants() {

    }

    @Override
    public Aggregate merge(Aggregate other) {
        return this;
    }

    @Override
    public Set<String> getEventSubscriptions() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getFieldsAbleToChange() {
        return null;
    }

    @Override
    public Set<String> getIntentionFields() {
        return null;
    }

    @Override
    public Aggregate mergeFields(Set<String> toCommitVersionChangedFields, Aggregate committedVersion, Set<String> committedVersionChangedFields) {
        return null;
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

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public QuestionCourse getCourse() {
        return course;
    }

    public void setCourse(QuestionCourse course) {
        this.course = course;
    }

    public Set<QuestionTopic> getTopics() {
        return topics;
    }

    public void setTopics(Set<QuestionTopic> topics) {
        this.topics = topics;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public void update(QuestionDto questionDto) {
        setTitle(questionDto.getTitle());
        setContent(questionDto.getContent());
        setOptions(questionDto.getOptionDtos().stream().map(Option::new).collect(Collectors.toList()));
    }
}
