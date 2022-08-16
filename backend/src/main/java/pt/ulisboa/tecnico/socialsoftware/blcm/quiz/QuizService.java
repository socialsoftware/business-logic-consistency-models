package pt.ulisboa.tecnico.socialsoftware.blcm.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;

import javax.transaction.Transactional;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.QUIZ_NOT_FOUND;

@Service
public class QuizService {

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private QuizRepository quizRepository;
    @Transactional
    public QuizDto generateQuiz(Integer numberOfQuestions, Set<Integer> topicsIds, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        Quiz quiz = new Quiz(numberOfQuestions, aggregateId, unitOfWork.getVersion());
        quizRepository.save(quiz);
        unitOfWork.addUpdatedObject(quiz, null);
        return new QuizDto(quiz);
    }

    @Transactional
    public QuizDto startTournamentQuiz(Integer userAggregateId, Integer quizAggregateId, UnitOfWork unitOfWork) {
        /* must add more verifications */
        Quiz oldQuiz = quizRepository.findByAggregateIdAndVersion(quizAggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(QUIZ_NOT_FOUND, quizAggregateId));

        Quiz newQuiz = new Quiz(oldQuiz);

        /*do stuff to the new quiz*/

        unitOfWork.addUpdatedObject(newQuiz, oldQuiz);
        return new QuizDto(oldQuiz);
    }
}
