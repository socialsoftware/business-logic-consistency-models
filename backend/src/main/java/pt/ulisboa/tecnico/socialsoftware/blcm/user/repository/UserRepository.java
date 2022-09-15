package pt.ulisboa.tecnico.socialsoftware.blcm.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;

import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Integer> {
    @Query(value = "select * from users u where u.aggregate_id = :aggregateId AND u.version < :maxVersion AND  u.version >= (select max(version) from users where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<User> findCausal(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from users u where u.course_execution_id = :executionAggregateId AND u.version < :maxVersion AND  u.version >= (select max(version) from users where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Set<User> findCausalByExecution(Integer executionAggregateId, Integer maxVersion);

    @Query(value = "select * from users u where u.aggregate_id = :aggregateId AND u.version >= :version", nativeQuery = true)
    Set<User> findConcurrentVersions(Integer aggregateId, Integer version);

    @Query(value = "select * from users u where aggregate_id NOT IN (select aggregate_id from users where state = 'DELETED')", nativeQuery = true)
    Set<User> findAllNonDeleted();
}
