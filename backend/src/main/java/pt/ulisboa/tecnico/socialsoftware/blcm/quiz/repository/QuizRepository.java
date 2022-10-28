package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.Quiz;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface QuizRepository extends JpaRepository<Quiz, Integer> {

    @Query(value = "select * from quizzes q where q.aggregate_id = :aggregateId AND q.version < :maxVersion AND q.state != 'DELETED' AND q.version >= (select max(version) from quizzes where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<Quiz> findCausal(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from quizzes q where q.aggregate_id = :aggregateId AND q.version < :maxVersion AND q.version >= (select max(version) from quizzes where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<Quiz> findCausalInactiveIncluded(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from quizzes where id = (select max(id) from quizzes where aggregate_id = :aggregateId AND version > :version)", nativeQuery = true)
    Optional<Quiz> findConcurrentVersions(Integer aggregateId, Integer version);

    @Query(value = "select q.aggregate_id from quizzes q where q.aggregate_id NOT IN (select aggregate_id from quizzes where state = 'DELETED' OR state = 'INACTIVE') AND (q.course_execution_aggregate_id = :courseExecutionAggregateId)", nativeQuery = true)
    Set<Integer> findAllAggregateIdsByCourseExecution(Integer courseExecutionAggregateId);

    @Query(value = "select q.aggregate_id from quizzes q, quiz_quiz_questions qqq where q.aggregate_id NOT IN (select aggregate_id from quizzes where state = 'DELETED' OR state = 'INACTIVE') AND (q.id = qqq.quiz_id) AND (:questionAggregateId IN (select question_aggregate_id from qqq))", nativeQuery = true)
    Set<Integer> findAllAggregateIdsByQuestion(Integer questionAggregateId);

    @Query(value = "select * from quizzes q where q.aggregate_id = :aggregateId AND state = 'ACTIVE' AND q.version >= (select max(version) from quizzes)", nativeQuery = true)
    Optional<Quiz> findLastQuestionVersion(Integer aggregateId);
}
