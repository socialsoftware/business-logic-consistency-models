package pt.ulisboa.tecnico.socialsoftware.blcm.execution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.event.publish.RemoveUserEvent;


@Service
public class CourseExecutionEventProcessing {
    @Autowired
    private CourseExecutionService courseExecutionService;
    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public void processRemoveUser(Integer aggregateId, RemoveUserEvent removeUserEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        System.out.printf("Processing remove user %d event for course execution %d\n", removeUserEvent.getPublisherAggregateId(), aggregateId);
        courseExecutionService.removeUser(aggregateId, removeUserEvent.getPublisherAggregateId(), removeUserEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }


}