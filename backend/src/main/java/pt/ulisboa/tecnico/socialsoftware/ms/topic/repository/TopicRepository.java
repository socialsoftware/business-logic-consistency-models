package pt.ulisboa.tecnico.socialsoftware.ms.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.ms.topic.domain.Topic;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Transactional
@Repository
public interface TopicRepository extends JpaRepository<Topic, Integer> {
    @Query(value = "select t1 from Topic t1 where t1.aggregateId = :aggregateId AND t1.state <> 'DELETED' AND t1.version = (select max(t2.version) from Topic t2 where t2.aggregateId = :aggregateId AND t2.version < :unitOfWorkVersion)")
    Optional<Topic> findCausal(Integer aggregateId, Integer unitOfWorkVersion);

    @Query(value = "select t1 from Topic t1  where t1.aggregateId = :aggregateId and t1.version = (select max(t2.version) from Topic t2 where t2.aggregateId = :aggregateId AND t2.version > :version)")
    Optional<Topic> findConcurrentVersions(Integer aggregateId, Integer version);
}
