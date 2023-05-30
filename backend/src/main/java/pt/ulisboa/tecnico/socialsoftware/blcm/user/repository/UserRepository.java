package pt.ulisboa.tecnico.socialsoftware.blcm.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;

import java.util.Optional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Integer> {
    @Query(value = "select u1 from User u1 where u1.aggregateId = :aggregateId AND u1.state <> 'DELETED' AND u1.version = (select max(u2.version) from User u2 where u2.aggregateId = :aggregateId AND u2.version < :unitOfWorkVersion)")
    Optional<User> findCausal(Integer aggregateId, Integer unitOfWorkVersion);

    @Query(value = "select u1 from User u1 where u1.aggregateId = :aggregateId and u1.version = (select max(u2.version) from User u2 where u2.aggregateId = :aggregateId and u2.version > :version)")
    Optional<User> findConcurrentVersions(Integer aggregateId, Integer version);
}
