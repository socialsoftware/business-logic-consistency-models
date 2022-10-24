package pt.ulisboa.tecnico.socialsoftware.blcm.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;

import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    @Query(value = "select * from questions q where q.aggregate_id = :aggregateId AND q.version < :maxVersion AND q.state = 'ACTIVE' AND q.version >= (select max(version) from questions where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<Question> findCausal(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from questions q where q.aggregate_id = :aggregateId AND q.version < :maxVersion AND q.version >= (select max(version) from questions where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<Question> findCausalInactiveIncluded(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from questions where id = (select max(id) from questions where aggregate_id = :aggregateId AND version > :version)", nativeQuery = true)
    Optional<Question> findConcurrentVersions(Integer aggregateId, Integer version);

    @Query(value = "select q.aggregate_id from questions q, question_topics qt where q.aggregate_id NOT IN (select aggregate_id from questions where state = 'DELETED' OR state = 'INACTIVE') AND (q.id = qt.question_id AND qt.topic_aggregate_id = :topicAggregateId)", nativeQuery = true)
    Set<Integer> findAllAggregateIdsByTopic(Integer topicAggregateId);

    @Query(value = "select * from questions q where q.aggregate_id = :aggregateId AND state = 'ACTIVE' AND q.version >= (select max(version) from questions)", nativeQuery = true)
    Optional<Question> findLastQuestionVersion(Integer aggregateId);
}