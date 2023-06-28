package pt.ulisboa.tecnico.socialsoftware.ms.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.question.domain.Question;

import java.util.Optional;

@Repository
@Transactional
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    @Query(value = "select q1 from Question q1 where q1.aggregateId = :aggregateId AND q1.state <> 'DELETED' AND q1.version = (select max(q2.version) from Question q2 where q2.aggregateId = :aggregateId AND q2.version < :unitOfWorkVersion)")
    Optional<Question> findCausal(Integer aggregateId, Integer unitOfWorkVersion);

    @Query(value = "select q1 from Question q1 where q1.aggregateId = :aggregateId and q1.version = (select max(q2.version) from Question q2 where q2.aggregateId = :aggregateId AND q2.version > :version)")
    Optional<Question> findConcurrentVersions(Integer aggregateId, Integer version);

    @Query(value = "select q1 from Question q1 where q1.aggregateId = :aggregateId AND q1.state = 'ACTIVE' AND q1.version = (select max(q2.version) from Question q2)")
    Optional<Question> findLastVersion(Integer aggregateId);

    @Query(value = "select q from Question q where q.aggregateId = :aggregateId AND q.version = :versionId")
    Optional<Question> findVersionByAggregateIdAndVersionId(Integer aggregateId, Integer versionId);
}