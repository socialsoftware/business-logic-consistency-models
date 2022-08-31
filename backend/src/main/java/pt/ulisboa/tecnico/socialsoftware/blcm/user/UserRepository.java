package pt.ulisboa.tecnico.socialsoftware.blcm.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Integer> {
    @Query(value = "select * from users t where t.aggregate_id = :aggregateId AND t.version <= :maxVersion AND t.state != 'INACTIVE'", nativeQuery = true)
    Optional<User> findByAggregateIdAndVersion(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from users t where t.course_execution_id = :executionAggregateId AND t.version <= :maxVersion AND t.state != 'INACTIVE'", nativeQuery = true)
    Set<User> findByExecutionAggregateIdAndVersion(Integer executionAggregateId, Integer maxVersion);

    @Query(value = "select * from users u where u.aggregate_id = :aggregateId AND u.version > :version AND u.state != 'INACTIVE'", nativeQuery = true)
    Set<User> findConcurrentVersions(Integer aggregateId, Integer version);
}
