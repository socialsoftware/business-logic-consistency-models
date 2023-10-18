package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.causal.coordination.eventProcessing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.ms.causal.unityOfWork.CausalUnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.causal.unityOfWork.CausalUnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.user.events.publish.DeleteUserEvent;

@Service
public class CourseExecutionEventProcessing {
    @Autowired
    private CourseExecutionService courseExecutionService;
    @Autowired
    private CausalUnitOfWorkService unitOfWorkService;

    public void processDeleteUserEvent(Integer aggregateId, DeleteUserEvent deleteUserEvent) {
        CausalUnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        courseExecutionService.removeUser(aggregateId, deleteUserEvent.getPublisherAggregateId(), deleteUserEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }


}
