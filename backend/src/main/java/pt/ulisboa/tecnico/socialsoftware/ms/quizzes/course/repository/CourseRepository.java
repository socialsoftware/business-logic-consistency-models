package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.domain.Course;

import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface CourseRepository extends JpaRepository<Course, Integer> {
    @Query(value = "select c1.id from Course c1 where c1.name = :courseName AND c1.state = 'ACTIVE'")
    Optional<Integer> findCourseIdByName(String courseName);
}
