package pt.ulisboa.tecnico.socialsoftware.ms.answer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.ms.answer.domain.QuizAnswer;

import jakarta.transaction.Transactional;

import java.util.Optional;

@Transactional
@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Integer> {
    @Query(value = "select a1 from QuizAnswer a1 where a1.aggregateId = :aggregateId AND a1.state <> 'DELETED' AND a1.version = (select max(a2.version) from QuizAnswer a2 where a2.aggregateId = :aggregateId AND a2.version < :unitOfWorkVersion)")
    Optional<QuizAnswer> findCausal(Integer aggregateId, Integer unitOfWorkVersion);

    @Query(value = "select a1 from QuizAnswer a1 where a1.aggregateId = :aggregateId and a1.version = (select max(a2.version) from QuizAnswer a2 where a2.aggregateId = :aggregateId AND a2.version > :version)")
    Optional<QuizAnswer> findConcurrentVersions(Integer aggregateId, Integer version);

    @Query(value = "select a1 from QuizAnswer a1 where a1.aggregateId = :aggregateId AND a1.state = 'ACTIVE' AND a1.version = (select max(a2.version) from QuizAnswer a2)")
    Optional<QuizAnswer> findLastAnswerVersion(Integer aggregateId);

    @Query(value = "select qa from QuizAnswer qa where qa.aggregateId = :aggregateId AND qa.version = :versionId")
    Optional<QuizAnswer> findVersionByAggregateIdAndVersionId(Integer aggregateId, Integer versionId);

    @Query(value = "select a1 from QuizAnswer a1 where a1.quiz.quizAggregateId = :quizAggregateId AND a1.student.studentAggregateId = :studentAggregateId AND a1.version = (select max(a2.version) from QuizAnswer a2 where a2.quiz.quizAggregateId = :quizAggregateId AND a2.student.studentAggregateId = :studentAggregateId AND a2.version < :unitOfWorkVersion)")
    Optional<QuizAnswer> findCausalByQuizAndUser(Integer quizAggregateId, Integer studentAggregateId, Integer unitOfWorkVersion);
}
