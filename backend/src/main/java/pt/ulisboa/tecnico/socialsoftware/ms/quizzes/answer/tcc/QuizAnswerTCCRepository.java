package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.tcc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.util.Optional;

@Transactional
@Repository
public interface QuizAnswerTCCRepository extends JpaRepository<QuizAnswerTCC, Integer> {
    @Query(value = "select a1 from QuizAnswerTCC a1 where a1.quiz.quizAggregateId = :quizAggregateId AND a1.student.studentAggregateId = :studentAggregateId AND a1.version = (select max(a2.version) from QuizAnswerTCC a2 where a2.quiz.quizAggregateId = :quizAggregateId AND a2.student.studentAggregateId = :studentAggregateId AND a2.version < :unitOfWorkVersion)")
    Optional<QuizAnswerTCC> findCausalQuizAnswerByQuizAndUser(Integer quizAggregateId, Integer studentAggregateId, Integer unitOfWorkVersion);
}
