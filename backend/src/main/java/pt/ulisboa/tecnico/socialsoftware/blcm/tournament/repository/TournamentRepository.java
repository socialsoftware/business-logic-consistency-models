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
    @Query(value = "select * from tournaments t where t.arggregate_id = :aggregateId AND t.version <= :maxVersion", nativeQuery = true)
    Optional<Tournament> findByAggregateIdAndVersion(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from tournaments t where t.aggregate_id = :aggregateId AND t.version > :version AND t.state != 'INACTIVE'", nativeQuery = true)
    Set<Tournament> findConcurrentVersions(Integer aggregateId, Integer version);

    /* may later be changed to */
}
