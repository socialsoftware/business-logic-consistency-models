package pt.ulisboa.tecnico.socialsoftware.ms.question.domain;

import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.question.event.subscribe.QuestionSubscribesDeleteTopic;
import pt.ulisboa.tecnico.socialsoftware.ms.question.event.subscribe.QuestionSubscribesUpdateTopic;
import pt.ulisboa.tecnico.socialsoftware.ms.question.dto.QuestionDto;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.aggregate.domain.Aggregate.AggregateState.ACTIVE;
import static pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.aggregate.domain.AggregateType.QUESTION;

/*
    INTRA-INVARIANTS:

    INTER-INVARIANTS:
        TOPICS_EXIST
        COURSE_EXISTS (course does not send events)
        COURSE_SAME_TOPIC_COURSE ()
 */
@Entity
public class Question extends Aggregate {
    private String title;
    private String content;
    private LocalDateTime creationDate;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "question")
    private QuestionCourse questionCourse;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "question")
    private Set<QuestionTopic> questionTopics = new HashSet<>();
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "question")
    private List<Option> options = new ArrayList<>();

    public Question() {

    }

    public Question(Integer aggregateId, QuestionCourse questionCourse, QuestionDto questionDto, List<QuestionTopic> questionTopics) {
        super(aggregateId, QUESTION);
        setTitle(questionDto.getTitle());
        setContent(questionDto.getContent());
        setCreationDate(LocalDateTime.now());
        setQuestionCourse(questionCourse);
        setOptions(questionDto.getOptionDtos().stream().map(Option::new).collect(Collectors.toList()));

        Integer optionKeyGenerator = 1;
        for (Option o : getOptions()) {
            o.setOptionKey(optionKeyGenerator++);
        }

        setQuestionTopics(new HashSet<>(questionTopics));
        setPrev(null);
    }

    public Question(Question other) {
        super(other);
        setTitle(other.getTitle());
        setContent(other.getContent());
        setCreationDate(other.getCreationDate());
        setQuestionCourse(new QuestionCourse(other.getQuestionCourse()));
        setOptions(other.getOptions().stream().map(Option::new).collect(Collectors.toList()));
        setQuestionTopics(other.getQuestionTopics().stream().map(QuestionTopic::new).collect(Collectors.toSet()));
    }


    @Override
    public void verifyInvariants() {

    }

    @Override
    public Set<EventSubscription> getEventSubscriptions() {
        //return Set.of(UPDATE_TOPIC, DELETE_TOPIC);
        Set<EventSubscription> eventSubscriptions = new HashSet<>();
        if (getState() == ACTIVE) {
            interInvariantTopicsExist(eventSubscriptions);
        }
        return eventSubscriptions;
    }

    private void interInvariantTopicsExist(Set<EventSubscription> eventSubscriptions) {
        for (QuestionTopic topic : this.questionTopics) {
            eventSubscriptions.add(new QuestionSubscribesDeleteTopic(topic));
            eventSubscriptions.add(new QuestionSubscribesUpdateTopic(topic));
        }
    }

    @Override
    public Set<String> getFieldsChangedByFunctionalities() {
        return Set.of("title", "content", "options", "questionTopics");
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

        Set<QuestionTopic> prevTopicsPre = new HashSet<>(prev.getQuestionTopics());
        Set<QuestionTopic> v1TopicsPre = new HashSet<>(v1.getQuestionTopics());
        Set<QuestionTopic> v2TopicsPre = new HashSet<>(v2.getQuestionTopics());

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
        mergedTournament.setQuestionTopics(mergedTopics);
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

    public QuestionCourse getQuestionCourse() {
        return questionCourse;
    }

    public void setQuestionCourse(QuestionCourse course) {
        this.questionCourse = course;
        this.questionCourse.setQuestion(this);
    }

    public Set<QuestionTopic> getQuestionTopics() {
        return questionTopics;
    }

    public void setQuestionTopics(Set<QuestionTopic> topics) {
        this.questionTopics = topics;
        this.questionTopics.forEach(questionTopic -> questionTopic.setQuestion(this));
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
        this.options.forEach(option -> option.setQuestion(this));
    }

    public void update(QuestionDto questionDto) {
        setTitle(questionDto.getTitle());
        setContent(questionDto.getContent());
        setOptions(questionDto.getOptionDtos().stream().map(Option::new).collect(Collectors.toList()));
    }

    public QuestionTopic findTopic(Integer topicAggregateId) {
        for(QuestionTopic questionTopic : this.questionTopics) {
            if(questionTopic.getTopicAggregateId().equals(topicAggregateId)) {
                return questionTopic;
            }
        }
        return null;
    }
}
