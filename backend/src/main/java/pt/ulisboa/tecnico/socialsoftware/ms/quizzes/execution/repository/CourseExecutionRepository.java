package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.domain.CourseExecution;

@Repository
@Transactional
public interface CourseExecutionRepository extends JpaRepository<CourseExecution, Integer> {
}
