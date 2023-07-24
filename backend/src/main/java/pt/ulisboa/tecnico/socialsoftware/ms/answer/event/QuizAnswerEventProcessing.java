package pt.ulisboa.tecnico.socialsoftware.ms.answer.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.ms.answer.service.QuizAnswerService;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.UnerollStudentFromCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.UpdateStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.question.event.publish.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.user.event.publish.RemoveUserEvent;

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
