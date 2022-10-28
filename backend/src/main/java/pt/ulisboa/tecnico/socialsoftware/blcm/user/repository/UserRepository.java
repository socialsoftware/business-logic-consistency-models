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

    static String NON_ACTIVE_USERS = "SELECT aggregate_id from users where state != 'ACTIVE'";
    @Query(value = "select * from users u where u.aggregate_id = :aggregateId AND u.version < :maxVersion AND u.state != 'DELETED' AND  u.version >= (select max(version) from users where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<User> findCausal(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from users u where u.aggregate_id = :aggregateId AND u.version < :maxVersion AND  u.version >= (select max(version) from users where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<User> findCausalInactiveIncluded(Integer aggregateId, Integer maxVersion);

    @Query(value = "select u.* from users u, user_course_executions uce where u.id = uce.user_id AND uce.course_execution_aggregate_id = :executionAggregateId AND u.id IN (select max(id) from users where version < :maxVersion AND aggregate_id NOT IN (" + NON_ACTIVE_USERS + ") group by aggregate_id)", nativeQuery = true)
    Set<User> findCausalByExecution(Integer executionAggregateId, Integer maxVersion);

    @Query(value = "select * from users u where u.id = (SELECT max(id) from users where u.aggregate_id = :aggregateId AND u.version > :version)", nativeQuery = true)
    Optional<User> findConcurrentVersions(Integer aggregateId, Integer version);

    @Query(value = "select * from users u where aggregate_id NOT IN (select aggregate_id from users where state = 'DELETED' OR state = 'INACTIVE')", nativeQuery = true)
    Set<User> findAllActive();

    @Query(value = "select u.aggregate_id from users u, user_course_executions uce where u.aggregate_id NOT IN (select aggregate_id from users where state = 'DELETED' OR state = 'INACTIVE') AND (uce.course_execution_aggregate_id = :courseExecutionAggregateId AND u.id = uce.user_id)", nativeQuery = true)
    Set<Integer> findAllAggregateIdsByCourseExecution(Integer courseExecutionAggregateId);
}
