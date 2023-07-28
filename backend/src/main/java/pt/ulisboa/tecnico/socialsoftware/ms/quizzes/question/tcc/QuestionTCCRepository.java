package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.tcc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface QuestionTCCRepository extends JpaRepository<QuestionTCC, Integer> {
}