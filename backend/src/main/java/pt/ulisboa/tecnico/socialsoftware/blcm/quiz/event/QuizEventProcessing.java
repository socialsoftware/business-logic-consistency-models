package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.event.publish.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.event.publish.UpdateQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;

@Service
public class QuizEventProcessing {
    private static final Logger logger = LoggerFactory.getLogger(QuizEventProcessing.class);

    @Autowired
    private QuizService quizService;
    @Autowired
    private UnitOfWorkService unitOfWorkService;


    public void processRemoveCourseExecutionEvent(Integer aggregateId, RemoveCourseExecutionEvent removeCourseExecutionEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("Processing remove course execution {} event for quiz {}", removeCourseExecutionEvent.getPublisherAggregateId(), aggregateId);
        quizService.removeCourseExecution(aggregateId, removeCourseExecutionEvent.getPublisherAggregateId(), removeCourseExecutionEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processUpdateQuestion(Integer aggregateId, UpdateQuestionEvent updateQuestionEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("Processing update update question {} event for quiz {}", updateQuestionEvent.getPublisherAggregateId(), aggregateId);
        quizService.updateQuestion(aggregateId, updateQuestionEvent.getPublisherAggregateId(), updateQuestionEvent.getTitle(), updateQuestionEvent.getContent(), updateQuestionEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void processRemoveQuizQuestion(Integer aggregateId, RemoveQuestionEvent removeQuestionEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("Processing remove quiz question execution {} event for quiz {}", removeQuestionEvent.getPublisherAggregateId(), aggregateId);
        quizService.removeQuizQuestion(aggregateId, removeQuestionEvent.getPublisherAggregateId(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
}
