package pt.ulisboa.tecnico.socialsoftware.blcm.answer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.Answer;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Transactional
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    @Query(value = "select * from answers a where a.aggregate_id = :aggregateId AND a.version < :maxVersion AND  a.version >= (select max(version) from answers where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<Answer> findCausal(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from answers q where q.quiz_aggregate_id = :quizAggregateId AND q.user_aggregate_id = :userAggregateId AND q.version < :maxVersion AND  q.version >= (select max(version) from answers where q.quiz_aggregate_id = :quizAggregateId AND q.user_aggregate_id = :userAggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<Answer> findCausalByQuizAndUser(Integer quizAggregateId, Integer userAggregateId, Integer maxVersion);


    @Query(value = "select * from answers where id = (select max(id) from answers where aggregate_id = :aggregateId AND version > :version)", nativeQuery = true)
    Optional<Answer> findConcurrentVersions(Integer aggregateId, Integer version);
}
