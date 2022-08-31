package pt.ulisboa.tecnico.socialsoftware.blcm.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;

import javax.transaction.Transactional;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

@Service
public class QuizService {

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private QuizRepository quizRepository;

    @Transactional
   public QuizDto getCausalQuiz(Integer aggregateId) {
        // TODO
        return new QuizDto(new Quiz());
    }

    @Transactional
    public QuizDto getCausalQuizRemote(Integer aggregateId, UnitOfWork unitOfWorkWorkService) {
        return new QuizDto(getCausalQuizLocal(aggregateId, unitOfWorkWorkService));
    }

    // intended for requests from local functionalities
    public Quiz getCausalQuizLocal(Integer aggregateId, UnitOfWork unitOfWorkWorkService) {
        Quiz quiz = quizRepository.findByAggregateIdAndVersion(aggregateId, unitOfWorkWorkService.getVersion())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, aggregateId));

        if(quiz.getState().equals(DELETED)) {
            throw new TutorException(TOURNAMENT_DELETED, quiz.getAggregateId());
        }

        quiz.checkDependencies(unitOfWorkWorkService);
        return quiz;
    }

    @Transactional
    public QuizDto generateQuiz(Integer numberOfQuestions, Set<Integer> topicsIds, UnitOfWork unitOfWorkWorkService) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        Quiz quiz = new Quiz(numberOfQuestions, aggregateId, unitOfWorkWorkService.getVersion());
        quizRepository.save(quiz);
        unitOfWorkWorkService.addUpdatedObject(quiz, "Quiz");
        return new QuizDto(quiz);
    }

    @Transactional
    public QuizDto startTournamentQuiz(Integer userAggregateId, Integer quizAggregateId, UnitOfWork unitOfWorkWorkService) {
        /* must add more verifications */
        Quiz oldQuiz = quizRepository.findByAggregateIdAndVersion(quizAggregateId, unitOfWorkWorkService.getVersion())
                .orElseThrow(() -> new TutorException(QUIZ_NOT_FOUND, quizAggregateId));

        Quiz newQuiz = new Quiz(oldQuiz);

        /*do stuff to the new quiz*/

        unitOfWorkWorkService.addUpdatedObject(newQuiz, "Quiz");
        return new QuizDto(oldQuiz);
    }
}
