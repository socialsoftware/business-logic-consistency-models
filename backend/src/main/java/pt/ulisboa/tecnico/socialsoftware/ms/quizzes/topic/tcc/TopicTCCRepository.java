package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.tcc;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.tcc.TopicTCC;

@Transactional
@Repository
public interface TopicTCCRepository extends JpaRepository<TopicTCC, Integer> {
}
