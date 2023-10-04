package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.topic.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.topic.domain.Topic;

@Transactional
@Repository
public interface TopicRepository extends JpaRepository<Topic, Integer> {
}
