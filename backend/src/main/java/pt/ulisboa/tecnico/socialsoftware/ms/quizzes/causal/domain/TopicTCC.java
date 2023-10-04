package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.causal.domain;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.causal.CausalConsistency;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.question.domain.QuestionTopic;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.topic.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.topic.domain.TopicCourse;

import java.util.HashSet;
import java.util.Set;

@Entity
public class TopicTCC extends Topic implements CausalConsistency {
    public TopicTCC() {
        super();
    }

    public TopicTCC(Integer aggregateId, String name, TopicCourse topicCourse) {
        super(aggregateId, name, topicCourse);
    }

    public TopicTCC(TopicTCC other) {
        super(other);
    }

    @Override
    public Set<String> getFieldsChangedByFunctionalities() {
        return Set.of("name");
    }

    @Override
    public Set<String[]> getIntentions() {
        return new HashSet<>();
    }

    @Override
    public Aggregate mergeFields(Set<String> toCommitVersionChangedFields, Aggregate committedVersion, Set<String> committedVersionChangedFields) {
        TopicTCC mergedTopic = new TopicTCC(this);
        TopicTCC committedTopic = (TopicTCC) committedVersion;
        mergeName(toCommitVersionChangedFields, mergedTopic, committedTopic);
        return mergedTopic;
    }

    private void mergeName(Set<String> toCommitVersionChangedFields, Topic mergedTopic, Topic committedTopic) {
        if (toCommitVersionChangedFields.contains("name")) {
            mergedTopic.setName(getName());
        } else {
            mergedTopic.setName(committedTopic.getName());
        }
    }
}
