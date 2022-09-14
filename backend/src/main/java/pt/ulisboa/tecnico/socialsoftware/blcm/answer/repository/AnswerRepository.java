package pt.ulisboa.tecnico.socialsoftware.blcm.answer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Transactional;
import java.util.Optional;

@Transactional
@Repository
public interface AnswerRepository extends JpaRepository<QuizAnswer, Integer> {
    @Query(value = "select * from quiz_answers qa where qa.aggregate_id = :aggregateId AND qa.version < :maxVersion AND  qa.version >= (select max(version) from quiz_answers where aggregate_id = :aggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<QuizAnswer> findByAggregateIdAndVersion(Integer aggregateId, Integer maxVersion);

    @Query(value = "select * from quiz_answers qa where qa.quiz_aggregate_id = :quizAggregateId AND qa.user_aggregate_id = :userAggregateId AND qa.version < :maxVersion AND  qa.version >= (select max(version) from quiz_answers where qa.quiz_aggregate_id = :quizAggregateId AND qa.user_aggregate_id = :userAggregateId AND version < :maxVersion)", nativeQuery = true)
    Optional<QuizAnswer> findByQuizUserAndVersion(Integer quizAggregateId, Integer userAggregateId, Integer maxVersion);


}
