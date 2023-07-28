package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.domain.Quiz;

@Repository
@Transactional
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
}
