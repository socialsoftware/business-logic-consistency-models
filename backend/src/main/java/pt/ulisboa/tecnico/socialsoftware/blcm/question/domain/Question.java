package pt.ulisboa.tecnico.socialsoftware.blcm.question.domain;

import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.ACTIVE;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.QUESTION;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.DELETE_TOPIC;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.UPDATE_TOPIC;

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

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<QuestionTopic> topics;

    @ElementCollection(fetch = FetchType.EAGER)
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
        super(other);
        setTitle(other.getTitle());
        setContent(other.getContent());
        setCreationDate(other.getCreationDate());
        setCourse(new QuestionCourse(other.getCourse()));
        setOptions(other.getOptions().stream().map(Option::new).collect(Collectors.toList()));
        setTopics(other.getTopics().stream().map(QuestionTopic::new).collect(Collectors.toSet()));
    }


    @Override
    public void verifyInvariants() {

    }

    @Override
    public Set<EventSubscription> getEventSubscriptions() {
        //return Set.of(UPDATE_TOPIC, DELETE_TOPIC);
        Set<EventSubscription> eventSubscriptions = new HashSet<>();
        if(getState() == ACTIVE) {
            for (QuestionTopic topic : this.topics) {
                interInvariantTopicsExist(eventSubscriptions, topic);
            }
        }
        return eventSubscriptions;
    }

    private void interInvariantTopicsExist(Set<EventSubscription> eventSubscriptions, QuestionTopic topic) {
        eventSubscriptions.add(new EventSubscription(topic.getAggregateId(), topic.getVersion(), DELETE_TOPIC, this));
        eventSubscriptions.add(new EventSubscription(topic.getAggregateId(), topic.getVersion(), UPDATE_TOPIC, this));
    }

    @Override
    public Set<String> getFieldsChangedByFunctionalities() {
        return Set.of("title", "content", "options", "topics");
    }

    @Override
    public Set<String[]> getIntentions() {

        return Set.of(
                new String[]{"title", "content"},
                new String[]{"title", "options"},
                new String[]{"content", "options"}
        );
    }

    @Override
    public Aggregate mergeFields(Set<String> toCommitVersionChangedFields, Aggregate committedVersion, Set<String> committedVersionChangedFields) {
        Question mergedQuestion = new Question(this);
        Question committedQuestion = (Question) committedVersion;

        mergeTitle(toCommitVersionChangedFields, mergedQuestion, committedQuestion);
        mergeContent(toCommitVersionChangedFields, mergedQuestion, committedQuestion);
        mergeOptions(toCommitVersionChangedFields, mergedQuestion, committedQuestion);
        mergeTopics((Question)getPrev(), this, committedQuestion, mergedQuestion);
        return mergedQuestion;
    }

    private void mergeTitle(Set<String> toCommitVersionChangedFields, Question mergedQuestion, Question committedQuestion) {
        if(toCommitVersionChangedFields.contains("title")) {
            mergedQuestion.setTitle(getTitle());
        } else {
            mergedQuestion.setTitle(committedQuestion.getTitle());
        }
    }

    private void mergeContent(Set<String> toCommitVersionChangedFields, Question mergedQuestion, Question committedQuestion) {
        if(toCommitVersionChangedFields.contains("content")) {
            mergedQuestion.setContent(getContent());
        } else {
            mergedQuestion.setContent(committedQuestion.getContent());
        }
    }

    private void mergeOptions(Set<String> toCommitVersionChangedFields, Question mergedQuestion, Question committedQuestion) {
        if(toCommitVersionChangedFields.contains("options")) {
            mergedQuestion.setOptions(getOptions());
        } else {
            mergedQuestion.setOptions(committedQuestion.getOptions());
        }
    }

    private static void mergeTopics(Question prev, Question v1, Question v2, Question mergedTournament) {
        /* Here we "calculate" the result of the incremental fields. This fields will always be the same regardless
         * of the base we choose. */

        Set<QuestionTopic> prevTopicsPre = new HashSet<>(prev.getTopics());
        Set<QuestionTopic> v1TopicsPre = new HashSet<>(v1.getTopics());
        Set<QuestionTopic> v2TopicsPre = new HashSet<>(v2.getTopics());

        QuestionTopic.syncTopicVersions(prevTopicsPre, v1TopicsPre, v2TopicsPre);

        Set<QuestionTopic> prevTopics = new HashSet<>(prevTopicsPre);
        Set<QuestionTopic> v1Topics = new HashSet<>(v1TopicsPre);
        Set<QuestionTopic> v2Topics = new HashSet<>(v2TopicsPre);

        Set<QuestionTopic> addedTopics =  SetUtils.union(
                SetUtils.difference(v1Topics, prevTopics),
                SetUtils.difference(v2Topics, prevTopics)
        );

        Set<QuestionTopic> removedTopics = SetUtils.union(
                SetUtils.difference(prevTopics, v1Topics),
                SetUtils.difference(prevTopics, v2Topics)
        );

        Set<QuestionTopic> mergedTopics = SetUtils.union(SetUtils.difference(prevTopics, removedTopics), addedTopics);
        mergedTournament.setTopics(mergedTopics);
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

    public QuestionTopic findTopic(Integer topicAggregateId) {
        for(QuestionTopic questionTopic : this.topics) {
            if(questionTopic.getAggregateId().equals(topicAggregateId)) {
                return questionTopic;
            }
        }
        return null;
    }
}
