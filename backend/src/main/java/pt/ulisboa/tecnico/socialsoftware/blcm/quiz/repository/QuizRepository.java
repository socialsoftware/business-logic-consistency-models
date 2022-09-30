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

    @Query(value = "select * from quizzes q where q.aggregate_id = :aggregateId AND q.version < :maxVersion AND q.version >= (select max(version) from quizzes where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<Quiz> findCausal(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from quizzes where id = (select max(id) from quizzes where aggregate_id = :aggregateId AND version > :version)", nativeQuery = true)
    Optional<Quiz> findConcurrentVersions(Integer aggregateId, Integer version);
}
