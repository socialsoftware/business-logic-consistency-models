package pt.ulisboa.tecnico.socialsoftware.blcm.answer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.Answer;

import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.Set;

@Transactional
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    @Query(value = "select a1 from Answer a1 where a1.aggregateId = :aggregateId AND a1.state != 'DELETED' AND a1.version = (select max(a2.version) from Answer a2 where a2.aggregateId = :aggregateId AND a2.version < :unitOfWorkVersion)")
    Optional<Answer> findCausal(Integer aggregateId, Integer unitOfWorkVersion);

    @Query(value = "select a1 from Answer a1 where a1.aggregateId = :aggregateId and a1.version = (select max(a2.version) from Answer a2 where a2.aggregateId = :aggregateId AND a2.version > :version)")
    Optional<Answer> findConcurrentVersions(Integer aggregateId, Integer version);

    @Query(value = "select a1 from Answer a1 where a1.aggregateId = :aggregateId AND a1.state = 'ACTIVE' AND a1.version = (select max(a2.version) from Answer a2)")
    Optional<Answer> findLastQuestionVersion(Integer aggregateId);

    @Query(value = "select a1 from Answer a1 where a1.quiz.quizAggregateId = :quizAggregateId AND a1.user.userAggregateId = :userAggregateId AND a1.version = (select max(a2.version) from Answer a2 where a2.quiz.quizAggregateId = :quizAggregateId AND a2.user.userAggregateId = :userAggregateId AND a2.version < :unitOfWorkVersion)")
    Optional<Answer> findCausalByQuizAndUser(Integer quizAggregateId, Integer userAggregateId, Integer unitOfWorkVersion);
}
