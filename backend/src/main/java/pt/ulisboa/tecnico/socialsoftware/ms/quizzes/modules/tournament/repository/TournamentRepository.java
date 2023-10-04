package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.tournament.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.tournament.domain.Tournament;

import java.util.Set;

@Repository
@Transactional
public interface TournamentRepository extends JpaRepository<Tournament, Integer> {
    @Query(value = "select t1.aggregateId from Tournament t1 where t1.aggregateId NOT IN (select t2.aggregateId from Tournament t2 where t2.state = 'DELETED' OR t2.state = 'INACTIVE') and t1.tournamentCourseExecution.courseExecutionAggregateId = :executionAggregateId")
    Set<Integer> findAllTournamentIdsOfNotDeletedAndNotInactiveByCourseExecution(Integer executionAggregateId);
}
