package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.Quiz;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    @Query(value = "select * from quizzes q where q.aggregate_id = :aggregateId AND q.version >= (select max(version) from quizzes where aggregate_id = :aggregateId AND version < :maxVersion", nativeQuery = true)
    Optional<Quiz> findByAggregateId(Integer aggregateId);

    @Query(value = "select * from quizzes q where q.aggregate_id = :aggregateId AND q.version < :maxVersion AND t.state != 'INACTIVE' AND q.version >= (select max(version) from questions where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<Quiz> findByAggregateIdAndVersion(Integer aggregateId, Integer maxVersion);
}
