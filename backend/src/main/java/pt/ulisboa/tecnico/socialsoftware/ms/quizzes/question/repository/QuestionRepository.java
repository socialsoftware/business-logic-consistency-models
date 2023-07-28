package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.domain.Question;

@Repository
@Transactional
public interface QuestionRepository extends JpaRepository<Question, Integer> {
}
