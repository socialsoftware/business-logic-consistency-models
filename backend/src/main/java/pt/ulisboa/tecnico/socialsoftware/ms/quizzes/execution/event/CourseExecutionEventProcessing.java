package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.event.publish.RemoveUserEvent;

@Service
public class CourseExecutionEventProcessing {
    @Autowired
    private CourseExecutionService courseExecutionService;
    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public void processRemoveUserEvent(Integer aggregateId, RemoveUserEvent removeUserEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        courseExecutionService.removeUser(aggregateId, removeUserEvent.getPublisherAggregateId(), removeUserEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }


}
