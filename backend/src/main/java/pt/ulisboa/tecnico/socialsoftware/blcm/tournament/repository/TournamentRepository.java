package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;

import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface TournamentRepository extends JpaRepository<Tournament, Integer> {
    @Query(value = "select t1 from Tournament t1 where t1.aggregateId = :aggregateId and t1.state <> 'DELETED' and t1.version = (select max(t2.version) from Tournament t2 where t2.aggregateId = :aggregateId and t2.version < :unitOfWorkVersion)")
    Optional<Tournament> findCausal(Integer aggregateId, Integer unitOfWorkVersion);

    @Query(value = "select t1 from Tournament t1 where t1.aggregateId = :aggregateId and t1.version = (select max(t2.version) from Tournament t2 where t2.aggregateId = :aggregateId and t2.version > :version)")
    Optional<Tournament> findConcurrentVersions(Integer aggregateId, Integer version);

    @Query(value = "select t1 from Tournament t1 where t1.aggregateId = :aggregateId AND t1.state = 'ACTIVE' AND t1.version = (select max(t2.version) from Tournament t2)")
    Optional<Tournament> findLastVersion(Integer aggregateId);

    @Query(value = "select t from Tournament t where t.aggregateId = :aggregateId AND t.version = :versionId")
    Optional<Tournament> findVersionByAggregateIdAndVersionId(Integer aggregateId, Integer versionId);

    @Query(value = "select t1.aggregateId from Tournament t1 where t1.aggregateId NOT IN (select t2.aggregateId from Tournament t2 where t2.state = 'DELETED' OR t2.state = 'INACTIVE') and t1.tournamentCourseExecution.courseExecutionAggregateId = :executionAggregateId")
    Set<Integer> findAllAggregateIdsOfNotDeletedAndNotInactiveByCourseExecution(Integer executionAggregateId);
}
