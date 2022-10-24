package pt.ulisboa.tecnico.socialsoftware.blcm.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.Topic;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Set;

@Transactional
@Repository
public interface TopicRepository extends JpaRepository<Topic, Integer> {
    @Query(value = "select * from topics t where t.aggregate_id = :aggregateId AND t.version < :maxVersion AND t.state = 'ACTIVE' AND t.version >= (select max(version) from topics where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<Topic> findCausal(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from topics t where t.aggregate_id = :aggregateId AND t.version < :maxVersion AND t.version >= (select max(version) from topics where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<Topic> findCausalInactiveIncluded(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from topics  where id = (select max(id) from topics where aggregate_id = :aggregateId AND version > :version)", nativeQuery = true)
    Optional<Topic> findConcurrentVersions(Integer aggregateId, Integer version);
}
