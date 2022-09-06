package pt.ulisboa.tecnico.socialsoftware.blcm.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Set;

@Transactional
@Repository
public interface TopicRepository extends JpaRepository<Topic, Integer> {
    @Query(value = "select * from topics t where t.aggregate_id = :aggregateId AND t.version < :maxVersion AND t.state != 'INACTIVE' AND t.version >= (select max(version) from topics where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)

    Optional<Topic> findByAggregateIdAndVersion(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from topics t where t.aggregate_id = :aggregateId AND t.version >= :version AND t.state != 'INACTIVE'", nativeQuery = true)
    Set<Topic> findConcurrentVersions(Integer aggregateId, Integer version);
}
