package pt.ulisboa.tecnico.socialsoftware.blcm.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.domain.Course;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface CourseRepository extends JpaRepository<Course, Integer> {
    @Query(value = "select c1 from Course c1 where c1.aggregateId = :aggregateId AND c1.state <> 'DELETED' AND c1.version = (select max(c2.version) from Course c2 where c2.aggregateId = :aggregateId AND c2.version < :unitOfWorkVersion)")
    Optional<Course> findCausal(Integer aggregateId, Integer unitOfWorkVersion);

    @Query(value = "select c1 from Course c1 where c1.aggregateId = :aggregateId and c1.version = (select max(c2.version) from Course c2 where c2.aggregateId = :aggregateId AND c2.version > :version)")
    Optional<Course> findConcurrentVersions(Integer aggregateId, Integer version);

    @Query(value = "select c1 from Course c1 where c1.name = :courseName AND c1.state = 'ACTIVE' and c1.version = (select max(c2.version) from Course c2 where c2.name = :courseName AND c2.version < :unitOfWorkVersion)")
    Optional<Course> findCausalByName(String courseName, Integer unitOfWorkVersion);
}
