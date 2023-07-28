package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.tcc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.util.Set;

@Repository
@Transactional
public interface CourseExecutionTCCRepository extends JpaRepository<CourseExecutionTCC, Integer> {
    @Query(value = "select ce1.aggregateId from CourseExecutionTCC ce1 where ce1.aggregateId NOT IN (select ce2.aggregateId from CourseExecutionTCC ce2 where ce2.state = 'DELETED')")
    Set<Integer> findCourseExecutionIdsOfAllNonDeleted();
}
