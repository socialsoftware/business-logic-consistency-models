package pt.ulisboa.tecnico.socialsoftware.blcm.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;

import javax.transaction.Transactional;
import java.util.Set;

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
        unitOfWork.addUpdatedObject(quiz);
        return new QuizDto(quiz);
    }
}
