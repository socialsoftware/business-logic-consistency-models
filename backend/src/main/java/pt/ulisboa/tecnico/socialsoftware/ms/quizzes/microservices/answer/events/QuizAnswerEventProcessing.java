package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.answer.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.answer.service.QuizAnswerService;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.causal.unityOfWork.CausalUnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.causal.unityOfWork.CausalUnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.events.publish.DisenrollStudentFromCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.events.publish.UpdateStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.question.events.publish.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.user.events.publish.RemoveUserEvent;

@Service
public class QuizAnswerEventProcessing {
    @Autowired
    private QuizAnswerService quizAnswerService;
    @Autowired
    private CausalUnitOfWorkService unitOfWorkService;

    public void processRemoveUserEvent(Integer aggregateId, Event eventToProcess) {
        CausalUnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        RemoveUserEvent removeUserEvent = (RemoveUserEvent) eventToProcess;
        quizAnswerService.removeUser(aggregateId, removeUserEvent.getPublisherAggregateId(), removeUserEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processRemoveQuestionEvent(Integer aggregateId, RemoveQuestionEvent removeQuestionEvent) {
        CausalUnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        quizAnswerService.removeQuestion(aggregateId, removeQuestionEvent.getPublisherAggregateId(), removeQuestionEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processUnenrollStudentEvent(Integer aggregateId, DisenrollStudentFromCourseExecutionEvent disenrollStudentFromCourseExecutionEvent) {
        CausalUnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        quizAnswerService.removeUser(aggregateId, disenrollStudentFromCourseExecutionEvent.getPublisherAggregateId(), disenrollStudentFromCourseExecutionEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processUpdateExecutionStudentNameEvent(Integer subscriberAggregateId, UpdateStudentNameEvent updateStudentNameEvent) {
        CausalUnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        quizAnswerService.updateUserName(subscriberAggregateId, updateStudentNameEvent.getPublisherAggregateId(), updateStudentNameEvent.getPublisherAggregateVersion(), updateStudentNameEvent.getStudentAggregateId(), updateStudentNameEvent.getUpdatedName(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
}
