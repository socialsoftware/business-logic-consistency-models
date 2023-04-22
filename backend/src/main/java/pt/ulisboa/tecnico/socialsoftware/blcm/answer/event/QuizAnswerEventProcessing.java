package pt.ulisboa.tecnico.socialsoftware.blcm.answer.event;

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
    @Autowired
    private QuizAnswerService quizAnswerService;
    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public void processRemoveUserEvent(Integer aggregateId, Event eventToProcess) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        RemoveUserEvent removeUserEvent = (RemoveUserEvent) eventToProcess;
        quizAnswerService.removeUser(aggregateId, removeUserEvent.getPublisherAggregateId(), removeUserEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processRemoveQuestionEvent(Integer aggregateId, RemoveQuestionEvent removeQuestionEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        quizAnswerService.removeQuestion(aggregateId, removeQuestionEvent.getPublisherAggregateId(), removeQuestionEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processUnenrollStudentEvent(Integer aggregateId, UnerollStudentFromCourseExecutionEvent unerollStudentFromCourseExecutionEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        quizAnswerService.removeUser(aggregateId, unerollStudentFromCourseExecutionEvent.getPublisherAggregateId(), unerollStudentFromCourseExecutionEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processUpdateExecutionStudentNameEvent(Integer subscriberAggregateId, UpdateStudentNameEvent updateStudentNameEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        quizAnswerService.updateUserName(subscriberAggregateId, updateStudentNameEvent.getPublisherAggregateId(), updateStudentNameEvent.getPublisherAggregateVersion(), updateStudentNameEvent.getStudentAggregateId(), updateStudentNameEvent.getUpdatedName(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
}
