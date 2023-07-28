package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.tcc;

import jakarta.persistence.Entity;
import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.CausalConsistency;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.domain.QuestionCourse;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.domain.QuestionTopic;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.dto.QuestionDto;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class QuestionTCC extends Question implements CausalConsistency {
    public QuestionTCC() {
        super();
    }

    public QuestionTCC(Integer aggregateId, QuestionCourse questionCourse, QuestionDto questionDto, List<QuestionTopic> questionTopics) {
        super(aggregateId, questionCourse, questionDto, questionTopics);
    }

    public QuestionTCC(QuestionTCC other) {
        super(other);
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
        QuestionTCC mergedQuestion = new QuestionTCC(this);
        QuestionTCC committedQuestion = (QuestionTCC) committedVersion;

        mergeTitle(toCommitVersionChangedFields, mergedQuestion, committedQuestion);
        mergeContent(toCommitVersionChangedFields, mergedQuestion, committedQuestion);
        mergeOptions(toCommitVersionChangedFields, mergedQuestion, committedQuestion);
        mergeTopics((Question)getPrev(), this, committedQuestion, mergedQuestion);
        return mergedQuestion;
    }

    private void mergeTitle(Set<String> toCommitVersionChangedFields, Question mergedQuestion, Question committedQuestion) {
        if (toCommitVersionChangedFields.contains("title")) {
            mergedQuestion.setTitle(getTitle());
        } else {
            mergedQuestion.setTitle(committedQuestion.getTitle());
        }
    }

    private void mergeContent(Set<String> toCommitVersionChangedFields, Question mergedQuestion, Question committedQuestion) {
        if (toCommitVersionChangedFields.contains("content")) {
            mergedQuestion.setContent(getContent());
        } else {
            mergedQuestion.setContent(committedQuestion.getContent());
        }
    }

    private void mergeOptions(Set<String> toCommitVersionChangedFields, Question mergedQuestion, Question committedQuestion) {
        if (toCommitVersionChangedFields.contains("options")) {
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
}
