package pt.ulisboa.tecnico.socialsoftware.blcm.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    @Query(value = "select * from quizzes q where q.arggregate_id = :aggregateId AND q.state", nativeQuery = true)
    Optional<Quiz> findByAggregateId(Integer aggregateId);

    @Query(value = "select * from quizzes q where q.arggregate_id = :aggregateId AND q.version <= :maxVersion", nativeQuery = true)
    Optional<Quiz> findByAggregateIdAndVersion(Integer aggregateId, Integer maxVersion);
}
