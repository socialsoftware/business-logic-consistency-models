package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.tcc;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.tcc.QuizTCC;

import java.util.Set;


@Repository
@Transactional
public interface QuizTCCRepository extends JpaRepository<QuizTCC, Integer> {
    @Query(value = "select q1.aggregateId from QuizTCC q1 where q1.aggregateId NOT IN (select q2.aggregateId from QuizTCC q2 where q2.state = 'DELETED') AND q1.quizCourseExecution.courseExecutionAggregateId = :courseExecutionAggregateId")
    Set<Integer> findAllQuizIdsByCourseExecution(Integer courseExecutionAggregateId);
}
