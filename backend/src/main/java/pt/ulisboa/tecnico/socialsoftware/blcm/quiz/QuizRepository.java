package pt.ulisboa.tecnico.socialsoftware.blcm.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    @Query(value = "select * from tournaments t where t.arggregate_id = :aggregateId AND t.state", nativeQuery = true)
    Optional<Quiz> findByAggregateId(Integer aggregateId);
}
