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
    @Query(value = "select * from tournaments t where t.aggregate_id = :aggregateId AND state != 'INACTIVE' AND t.version < :maxVersion AND t.version >= (select max(version) from tournaments where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<Tournament> findCausal(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from tournaments where id = (select max(id) from tournaments where aggregate_id = :aggregateId AND version > :version)", nativeQuery = true)
    Optional<Tournament> findConcurrentVersions(Integer aggregateId, Integer version);

    @Query(value = "select * from tournaments t where aggregate_id NOT IN (select aggregate_id from tournaments where state = 'DELETED' OR state = 'INACTIVE')", nativeQuery = true)
    Set<Tournament> findAllActive();

    @Query(value = "select t.aggregate_id from tournaments t, tournament_participants tp where t.aggregate_id NOT IN (select aggregate_id from tournaments where state = 'DELETED' OR state = 'INACTIVE') AND ((t.id = tp.tournament_id AND tp.participant_aggregate_id = :userAggregateId)  OR (t.creator_aggregate_id = :userAggregateId))", nativeQuery = true)
    Set<Integer> findAllAggregateIdsByUser(Integer userAggregateId);

    @Query(value = "select t.aggregate_id from tournaments t, tournament_participants tp where t.aggregate_id NOT IN (select aggregate_id from tournaments where state = 'DELETED' OR state = 'INACTIVE') AND (t.course_execution_aggregate_id = :executionAggregateId) AND((t.id = tp.tournament_id AND tp.participant_aggregate_id = :userAggregateId)  OR (t.creator_aggregate_id = :userAggregateId))", nativeQuery = true)
    Set<Integer> findAllAggregateIdsByExecutionAndUser(Integer executionAggregateId, Integer userAggregateId);

    @Query(value = "select t.aggregate_id from tournaments t where t.aggregate_id NOT IN (select aggregate_id from tournaments where state = 'DELETED' OR state = 'INACTIVE') AND (t.course_execution_aggregate_id = :courseExecutionAggregateId)", nativeQuery = true)
    Set<Integer> findAllAggregateIdsByCourseExecution(Integer courseExecutionAggregateId);

    @Query(value = "select t.aggregate_id from tournaments t, tournament_topics tt where t.aggregate_id NOT IN (select aggregate_id from tournaments where state = 'DELETED' OR state = 'INACTIVE') AND (t.id = tt.tournament_id AND tt.topic_aggregate_id = :topicAggregateId)", nativeQuery = true)
    Set<Integer> findAllAggregateIdsByTopic(Integer topicAggregateId);

    @Query(value = "select t.aggregate_id from tournaments t where t.aggregate_id NOT IN (select aggregate_id from tournaments where state = 'DELETED' OR state = 'INACTIVE') AND (t.quiz_aggregate_id = :quizAggregateId)", nativeQuery = true)
    Set<Integer> findAllAggregateIdsByQuiz(Integer quizAggregateId);

    @Query(value = "select * from tournaments t where t.aggregate_id = :aggregateId AND state = 'ACTIVE' AND t.version >= (select max(version) from tournaments)", nativeQuery = true)
    Optional<Tournament> findLastTournamentVersion(Integer aggregateId);
}
