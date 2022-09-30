package pt.ulisboa.tecnico.socialsoftware.blcm.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.domain.Course;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface CourseRepository extends JpaRepository<Course, Integer> {
    @Query(value = "select * from courses c where c.aggregate_id = :aggregateId AND c.version < :maxVersion AND c.state != 'INACTIVE' AND c.version >= (select max(version) from courses where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<Course> findCausal(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from courses where id = (select max(id) from courses where aggregate_id = :aggregateId AND version > :version)", nativeQuery = true)
    Optional<Course> findConcurrentVersions(Integer aggregateId, Integer version);
    @Query(value = "select * from courses c where c.name = :courseName AND c.version <= :version AND c.state != 'INACTIVE'", nativeQuery = true)
    Optional<Course> findCausalByName(String courseName, Integer version);
}
