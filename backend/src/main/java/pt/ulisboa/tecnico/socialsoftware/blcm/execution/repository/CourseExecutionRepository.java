package pt.ulisboa.tecnico.socialsoftware.blcm.execution.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Set;


@Repository
@Transactional
public interface CourseExecutionRepository extends JpaRepository<CourseExecution, Integer> {
    @Query(value = "select * from course_executions ce where ce.aggregate_id = :aggregateId AND ce.version < :maxVersion AND ce.state != 'INACTIVE' AND ce.version >= (select max(version) from course_executions where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<CourseExecution> findCausal(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from course_executions where id = (select max(id) from course_executions where aggregate_id = :aggregateId AND version > :version)", nativeQuery = true)
    Optional<CourseExecution> findConcurrentVersions(Integer aggregateId, Integer version);

    @Query(value = "select * from course_executions ce where aggregate_id NOT IN (select aggregate_id from course_executions where state = 'DELETED')", nativeQuery = true)
    Set<CourseExecution> findAllNonDeleted();

    @Query(value = "select t.aggregate_id from course_executions ce, course_executions_studends ces where cr.aggregate_id NOT IN (select aggregate_id from course_executions where state = 'DELETED' OR state = 'INACTIVE') AND ((ce.id = ces.course_execution_id AND ces.user_aggregate_id = :userAggregateId)  OR (t.creator_aggregate_id = :userAggregateId))", nativeQuery = true)
    Set<Integer> findAllAggregateIdsByUser(Integer userAggregateId);
}
