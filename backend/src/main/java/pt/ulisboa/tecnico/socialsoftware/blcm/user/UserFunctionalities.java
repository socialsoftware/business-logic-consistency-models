package pt.ulisboa.tecnico.socialsoftware.blcm.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.service.UserService;

@Service
public class UserFunctionalities {

    @Autowired
    private UserService userService;

    public void anonymizeCourseExecutionUsers(Integer executionAggregateId) {
        UnitOfWork unitOfWork = new UnitOfWork();
        userService.anonymizeCourseExecutionUsers(executionAggregateId, unitOfWork);
        unitOfWork.commit();
    }
}
