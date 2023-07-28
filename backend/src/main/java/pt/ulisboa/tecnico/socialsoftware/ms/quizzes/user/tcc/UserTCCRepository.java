package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.tcc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.tcc.UserTCC;

@Repository
@Transactional
public interface UserTCCRepository extends JpaRepository<UserTCC, Integer> {
}
