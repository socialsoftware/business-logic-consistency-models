package pt.ulisboa.tecnico.socialsoftware.blcm.question;

import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.Dependency;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.AggregateType.COURSE;

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

    @ManyToOne(fetch = FetchType.LAZY)
    private Aggregate prev;

    public Question() {

    }

    public Question(Integer aggregateId, QuestionCourse course, QuestionDto questionDto) {
        super(aggregateId);
        setTitle(questionDto.getTitle());
        setContent(questionDto.getTitle());
        setCreationDate(LocalDateTime.now());
        setCourse(course);
        setOptions(questionDto.getOptionDto().stream().map(Option::new).collect(Collectors.toList()));
        setPrev(null);
    }

    public Question(Question other) {
        super(other.getAggregateId());
        setId(null);
        setTitle(other.getTitle());
        setContent(other.getContent());
        setCreationDate(other.getCreationDate());
        setCourse(other.getCourse());
        setOptions(other.getOptions());
        setTopics(new HashSet<>(other.getTopics()));
        setPrev(other);
    }


    @Override
    public boolean verifyInvariants() {
        return true;
    }

    @Override
    public Aggregate getPrev() {
        return prev;
    }

    @Override
    public Aggregate merge(Aggregate other) {
        return this;
    }

    @Override
    public Map<Integer, Dependency> getDependenciesMap() {
        Map<Integer, Dependency> depMap = new HashMap<>();

        depMap.put(this.course.getAggregateId(), new Dependency(this.course.getAggregateId(), COURSE, this.course.getVersion()));
        this.topics.forEach(t -> {
            depMap.put(t.getTopicAggregateId(), new Dependency(t.getTopicAggregateId(), COURSE, t.getVersion()));
        });
        return  depMap;
    }

    public void setPrev(Question prev) {
        this.prev = prev;
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
        setOptions(questionDto.getOptionDto().stream().map(Option::new).collect(Collectors.toList()));
    }
}
