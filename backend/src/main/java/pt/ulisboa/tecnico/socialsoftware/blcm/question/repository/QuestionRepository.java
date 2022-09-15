package pt.ulisboa.tecnico.socialsoftware.blcm.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.domain.Question;

import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    @Query(value = "select * from questions q where q.aggregate_id = :aggregateId AND q.version < :maxVersion AND q.state != 'INACTIVE' AND q.version >= (select max(version) from questions where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<Question> findCausal(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from questions q where q.aggregate_id = :aggregateId AND q.version >= :version AND q.state != 'INACTIVE'", nativeQuery = true)
    Set<Question> findConcurrentVersions(Integer aggregateId, Integer version);

}