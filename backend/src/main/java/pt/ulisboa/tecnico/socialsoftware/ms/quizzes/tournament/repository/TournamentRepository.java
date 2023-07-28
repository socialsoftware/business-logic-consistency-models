package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.domain.Tournament;

@Repository
@Transactional
public interface TournamentRepository extends JpaRepository<Tournament, Integer> {
}
