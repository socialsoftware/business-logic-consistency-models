package pt.ulisboa.tecnico.socialsoftware.blcm.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.AnonymizeUserEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.RemoveUserEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.Role;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.UserCourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Transactional
    public UserDto getCausalUserRemote(Integer aggregateId, UnitOfWork unitOfWorkWorkService) {
        return new UserDto(getCausalUserLocal(aggregateId, unitOfWorkWorkService));
    }

    // intended for requests from local functionalities
    public User getCausalUserLocal(Integer aggregateId, UnitOfWork unitOfWorkWorkService) {
        User user = userRepository.findByAggregateIdAndVersion(aggregateId, unitOfWorkWorkService.getVersion())
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, aggregateId));

        if(user.getState().equals(DELETED)) {
            throw new TutorException(ErrorMessage.USER_DELETED, user.getAggregateId());
        }

        user.checkDependencies(unitOfWorkWorkService);
        return user;
    }

    /*simple user creation*/
    @Transactional
    public UserDto createUser(UserDto userDto, UnitOfWork unitOfWorkWorkService) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        User user = new User(aggregateId, unitOfWorkWorkService.getVersion(), userDto);
        unitOfWorkWorkService.addUpdatedObject(user, "User");
        return new UserDto(user);
    }



    @Transactional
    public UserDto getUserById(Integer userId, UnitOfWork unitOfWorkWorkService) {
        return new UserDto();
    }

    @Transactional
    public void anonymizeCourseExecutionUsers(Integer executionAggregateId, UnitOfWork unitOfWorkWorkService) {
        Set<User> executionsUsers = userRepository.findByExecutionAggregateIdAndVersion(executionAggregateId, unitOfWorkWorkService.getVersion());
        executionsUsers.forEach(oldUser -> {
            User newUser = new User(oldUser);
            newUser.anonymize();
            unitOfWorkWorkService.addUpdatedObject(newUser, "User");
            unitOfWorkWorkService.addEvent(new AnonymizeUserEvent(newUser.getAggregateId()));
        });
    }

    @Transactional
    public void addCourseExecution(Integer userAggregateId, UserCourseExecution userCourseExecution, UnitOfWork unitOfWork) {
        User oldUser = getCausalUserLocal(userAggregateId, unitOfWork);

        if(!oldUser.isActive()){
            throw new TutorException(ErrorMessage.INACTIVE_USER);
        }

        User newUser = new User(oldUser);
        newUser.addCourseExecution(userCourseExecution);
        unitOfWork.addUpdatedObject(newUser, "User");
    }

    @Transactional
    public void activateUser(Integer userAggregateId, UnitOfWork unitOfWork) {
        User oldUser = getCausalUserLocal(userAggregateId, unitOfWork);
        if(oldUser.isActive()) {
            throw new TutorException(USER_ACTIVE);
        }
        User newUser = new User(oldUser);
        newUser.setActive(true);
        unitOfWork.addUpdatedObject(newUser, "User");
    }

    @Transactional
    public Set<CourseExecutionDto> getUserCourseExecutions(Integer userAggregateId, UnitOfWork unitOfWork) {
        User user = getCausalUserLocal(userAggregateId, unitOfWork);
        return user.getCourseExecutions().stream()
                .map(UserCourseExecution::buildDto)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void deleteUser(Integer userAggregateId, UnitOfWork unitOfWork) {
        User oldUser = getCausalUserLocal(userAggregateId, unitOfWork);
        User newUser = new User(oldUser);
        newUser.remove();
        userRepository.save(newUser);
        unitOfWork.addUpdatedObject(newUser, "User");
    }

    @Transactional
    public void removeCourseExecutionsFromUser(Integer userAggregateId, List<Integer> courseExecutionsAggregateIds, UnitOfWork unitOfWork) {
        User oldUser = getCausalUserLocal(userAggregateId, unitOfWork);
        User newUser = new User(oldUser);
        Set<UserCourseExecution> courseExecutionsToRemove = newUser.getCourseExecutions().stream()
                .filter(uce -> courseExecutionsAggregateIds.contains(uce))
                .collect(Collectors.toSet());

        for (UserCourseExecution userCourseExecution : courseExecutionsToRemove) {
            newUser.removeCourseExecution(userCourseExecution);
        }

        userRepository.save(newUser);
        unitOfWork.addEvent(new RemoveUserEvent(userAggregateId));
        unitOfWork.addUpdatedObject(newUser,"User");
    }

    @Transactional
    public List<UserDto> getStudents(UnitOfWork unitOfWork) {
        Set<Integer> studentsIds = userRepository.findAll().stream()
                .filter(u -> u.getRole().equals(Role.STUDENT))
                .map(User::getAggregateId)
                .collect(Collectors.toSet());
        return studentsIds.stream()
                .map(id -> getCausalUserLocal(id, unitOfWork))
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<UserDto> getTeachers(UnitOfWork unitOfWork) {
        Set<Integer> teacherIds = userRepository.findAll().stream()
                .filter(u -> u.getRole().equals(Role.TEACHER))
                .map(User::getAggregateId)
                .collect(Collectors.toSet());
        return teacherIds.stream()
                .map(id -> getCausalUserLocal(id, unitOfWork))
                .map(UserDto::new)
                .collect(Collectors.toList());
    }
}
