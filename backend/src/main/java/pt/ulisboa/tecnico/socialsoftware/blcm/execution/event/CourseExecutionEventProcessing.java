package pt.ulisboa.tecnico.socialsoftware.blcm.execution.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.event.publish.RemoveUserEvent;

@Service
public class CourseExecutionEventProcessing {
    private static final Logger logger = LoggerFactory.getLogger(CourseExecutionEventProcessing.class);

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
