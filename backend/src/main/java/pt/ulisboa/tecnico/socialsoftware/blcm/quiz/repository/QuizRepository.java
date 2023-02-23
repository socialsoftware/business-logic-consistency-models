package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.Quiz;

import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    @Query(value = "select q1 from Quiz q1 where q1.aggregateId = :aggregateId AND q1.state != 'DELETED' AND q1.version = (select max(q2.version) from Quiz q2 where q2.aggregateId = :aggregateId AND q2.version < :unitOfWorkVersion)")
    Optional<Quiz> findCausal(Integer aggregateId, Integer unitOfWorkVersion);

    @Query(value = "select q1 from Quiz q1 where q1.aggregateId = :aggregateId and q1.version = (select max(q2.version) from Quiz q2 where q2.aggregateId = :aggregateId AND q2.version > :version)")
    Optional<Quiz> findConcurrentVersions(Integer aggregateId, Integer version);

    @Query(value = "select q1 from Quiz q1 where q1.aggregateId = :aggregateId AND q1.state = 'ACTIVE' AND q1.version = (select max(q2.version) from Quiz q2)")
    Optional<Quiz> findLastVersion(Integer aggregateId);

    @Query(value = "select q from Quiz q where q.aggregateId = :aggregateId AND q.version = :versionId")
    Optional<Quiz> findVersionByAggregateIdAndVersionId(Integer aggregateId, Integer versionId);

    @Query(value = "select q1.aggregateId from Quiz q1 where q1.aggregateId NOT IN (select q2.aggregateId from Quiz q2 where q2.state = 'DELETED') AND q1.courseExecution.courseExecutionAggregateId = :courseExecutionAggregateId")
    Set<Integer> findAllAggregateIdsByCourseExecution(Integer courseExecutionAggregateId);
}
