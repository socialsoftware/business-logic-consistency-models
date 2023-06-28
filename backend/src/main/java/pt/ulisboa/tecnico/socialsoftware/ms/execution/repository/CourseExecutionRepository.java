package pt.ulisboa.tecnico.socialsoftware.ms.execution.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.domain.CourseExecution;

import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface CourseExecutionRepository extends JpaRepository<CourseExecution, Integer> {
    @Query(value = "select ce1 from CourseExecution ce1 where ce1.aggregateId = :aggregateId and ce1.state <> 'DELETED' and ce1.version = (select max(ce2.version) from CourseExecution ce2 where ce2.aggregateId = :aggregateId AND ce2.version < :unitOfWorkVersion)")
    Optional<CourseExecution> findCausal(Integer aggregateId, Integer unitOfWorkVersion);

    @Query(value = "select ce1 from CourseExecution ce1 where ce1.aggregateId = :aggregateId and ce1.version = (select max(ce2.version) from CourseExecution ce2 where ce2.aggregateId = :aggregateId AND ce2.version > :version)")
    Optional<CourseExecution> findConcurrentVersions(Integer aggregateId, Integer version);

    @Query(value = "select ce1 from CourseExecution ce1 where ce1.aggregateId = :aggregateId AND ce1.state = 'ACTIVE' AND ce1.version = (select max(ce2.version) from CourseExecution ce2)")
    Optional<CourseExecution> findLastVersion(Integer aggregateId);

    @Query(value = "select ce from CourseExecution ce where ce.aggregateId = :aggregateId AND ce.version = :versionId")
    Optional<CourseExecution> findVersionByAggregateIdAndVersionId(Integer aggregateId, Integer versionId);

    @Query(value = "select ce1.aggregateId from CourseExecution ce1 where ce1.aggregateId NOT IN (select ce2.aggregateId from CourseExecution ce2 where ce2.state = 'DELETED')")
    Set<Integer> findAggregateIdsOfAllNonDeleted();
}
