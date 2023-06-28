package pt.ulisboa.tecnico.socialsoftware.ms.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.ms.user.service.UserService;
import pt.ulisboa.tecnico.socialsoftware.ms.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.ms.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService;

import java.util.List;

@Service
public class UserFunctionalities {
    @Autowired
    private UserService userService;
    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public UserDto createUser(UserDto userDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        checkInput(userDto);

        UserDto userDto1 = userService.createUser(userDto, unitOfWork);

        unitOfWorkService.commit(unitOfWork);
        return userDto1;
    }

    public UserDto findByAggregateId(Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        return userService.getCausalUserRemote(userAggregateId, unitOfWork);
    }

    public void activateUser(Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        userService.activateUser(userAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void deleteUser(Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        userService.deleteUser(userAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public List<UserDto> getStudents() {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        return userService.getStudents(unitOfWork);
    }

    public List<UserDto> getTeachers() {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
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
