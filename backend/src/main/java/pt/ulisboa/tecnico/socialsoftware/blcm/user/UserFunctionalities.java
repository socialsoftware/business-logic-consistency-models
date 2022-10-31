package pt.ulisboa.tecnico.socialsoftware.blcm.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.UserCourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.service.UserService;

import java.util.List;
import java.util.Set;

@Service
public class UserFunctionalities {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseExecutionService courseExecutionService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public UserDto createUser(UserDto userDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        checkInput(userDto);

        UserDto userDto1 = userService.createUser(userDto, unitOfWork);

        unitOfWorkService.commit(unitOfWork);
        return userDto1;
    }

    public UserDto findByAggregateId(Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return userService.getCausalUserRemote(userAggregateId, unitOfWork);
    }

    public void activateUser(Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        userService.activateUser(userAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void deleteUser(Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        userService.deleteUser(userAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public List<UserDto> getStudents() {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return userService.getStudents(unitOfWork);
    }

    public List<UserDto> getTeachers() {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return userService.getTeachers(unitOfWork);
    }

    private void checkInput(UserDto userDto) {
        if (userDto.getName() == null) {
            throw new TutorException(ErrorMessage.USER_MISSING_NAME);
        }
        if (userDto.getUsername() == null) {
            throw new TutorException(ErrorMessage.USER_MISSING_USERNAME);
        }
        if (userDto.getRole() == null) {
            throw new TutorException(ErrorMessage.USER_MISSING_ROLE);
        }

    }
}
