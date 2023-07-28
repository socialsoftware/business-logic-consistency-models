package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.tcc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.tcc.CourseTCC;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface CourseTCCRepository extends JpaRepository<CourseTCC, Integer> {
    @Query(value = "select c1 from CourseTCC c1 where c1.name = :courseName AND c1.state = 'ACTIVE' and c1.version = (select max(c2.version) from CourseTCC c2 where c2.name = :courseName AND c2.version < :unitOfWorkVersion)")
    Optional<CourseTCC> findCausalCourseByName(String courseName, Integer unitOfWorkVersion);
}
