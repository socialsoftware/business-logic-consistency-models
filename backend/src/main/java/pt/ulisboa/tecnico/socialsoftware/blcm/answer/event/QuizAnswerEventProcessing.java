package pt.ulisboa.tecnico.socialsoftware.blcm.answer.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.service.QuizAnswerService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.UnerollStudentFromCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.UpdateStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.event.publish.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.event.publish.RemoveUserEvent;

@Service
public class QuizAnswerEventProcessing {
    private static final Logger logger = LoggerFactory.getLogger(QuizAnswerEventProcessing.class);

    @Autowired
    private QuizAnswerService quizAnswerService;
    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public void processRemoveUser(Integer aggregateId, Event eventToProcess) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("Processing remove user {} event for answer {}", eventToProcess.getPublisherAggregateId(), aggregateId);
        RemoveUserEvent removeUserEvent = (RemoveUserEvent) eventToProcess;
        quizAnswerService.removeUser(aggregateId, removeUserEvent.getPublisherAggregateId(), removeUserEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processRemoveQuestion(Integer aggregateId, RemoveQuestionEvent removeQuestionEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("Processing remove question {} event for answer {}", removeQuestionEvent.getPublisherAggregateId(), aggregateId);
        quizAnswerService.removeQuestion(aggregateId, removeQuestionEvent.getPublisherAggregateId(), removeQuestionEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processUnenrollStudent(Integer aggregateId, UnerollStudentFromCourseExecutionEvent unerollStudentFromCourseExecutionEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("Processing uneroll student from course execution {} event for answer {}", unerollStudentFromCourseExecutionEvent.getPublisherAggregateId(), aggregateId);
        quizAnswerService.removeUser(aggregateId, unerollStudentFromCourseExecutionEvent.getPublisherAggregateId(), unerollStudentFromCourseExecutionEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processUpdateExecutionStudentNameEvent(Integer subscriberAggregateId, UpdateStudentNameEvent updateStudentNameEvent) {
        logger.info("Processing update execution student name of execution {} event for answer {}", updateStudentNameEvent.getPublisherAggregateId(), subscriberAggregateId);
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        quizAnswerService.updateUserName(subscriberAggregateId, updateStudentNameEvent.getPublisherAggregateId(), updateStudentNameEvent.getPublisherAggregateVersion(), updateStudentNameEvent.getStudentAggregateId(), updateStudentNameEvent.getUpdatedName(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
}
