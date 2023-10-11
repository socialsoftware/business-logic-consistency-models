package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.causal.aggregates;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.aggregate.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.causal.aggregate.CausalAggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.aggregate.Topic;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.aggregate.TopicCourse;

import java.util.HashSet;
import java.util.Set;

@Entity
public class CausalTopic extends Topic implements CausalAggregate {
    public CausalTopic() {
        super();
    }

    public CausalTopic(Integer aggregateId, String name, TopicCourse topicCourse) {
        super(aggregateId, name, topicCourse);
    }

    public CausalTopic(CausalTopic other) {
        super(other);
    }

    @Override
    public Set<String> getMutableFields() {
        return Set.of("name");
    }

    @Override
    public Set<String[]> getIntentions() {
        return new HashSet<>();
    }

    @Override
    public Aggregate mergeFields(Set<String> toCommitVersionChangedFields, Aggregate committedVersion, Set<String> committedVersionChangedFields) {
        CausalTopic mergedTopic = new CausalTopic(this);
        CausalTopic committedTopic = (CausalTopic) committedVersion;
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
