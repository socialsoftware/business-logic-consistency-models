package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.service.CausalConsistencyService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.tcc.UserTCC;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.tcc.UserTCCRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.event.publish.RemoveUserEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.domain.Role;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.dto.UserDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.ErrorMessage.*;

@Service
public class UserService {
    @Autowired
    private UserTCCRepository userTCCRepository;
    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;
    @Autowired
    private CausalConsistencyService causalConsistencyService;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserDto getCausalUserRemote(Integer aggregateId, UnitOfWork unitOfWork) {
        return new UserDto((UserTCC) causalConsistencyService.addAggregateCausalSnapshot(aggregateId, unitOfWork));
    }

    /*simple user creation*/
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserDto createUser(UserDto userDto, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        UserTCC user = new UserTCC(aggregateId, userDto);
        unitOfWork.registerChanged(user);
        return new UserDto(user);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void activateUser(Integer userAggregateId, UnitOfWork unitOfWork) {
        UserTCC oldUser = (UserTCC) causalConsistencyService.addAggregateCausalSnapshot(userAggregateId, unitOfWork);
        if (oldUser.isActive()) {
            throw new TutorException(USER_ACTIVE);
        }
        UserTCC newUser = new UserTCC(oldUser);
        newUser.setActive(true);
        unitOfWork.registerChanged(newUser);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteUser(Integer userAggregateId, UnitOfWork unitOfWork) {
        UserTCC oldUser = (UserTCC) causalConsistencyService.addAggregateCausalSnapshot(userAggregateId, unitOfWork);
        UserTCC newUser = new UserTCC(oldUser);
        newUser.remove();
        unitOfWork.registerChanged(newUser);
        unitOfWork.addEvent(new RemoveUserEvent(newUser.getAggregateId()));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<UserDto> getStudents(UnitOfWork unitOfWork) {
        Set<Integer> studentsIds = userTCCRepository.findAll().stream()
                .filter(u -> u.getRole().equals(Role.STUDENT))
                .map(User::getAggregateId)
                .collect(Collectors.toSet());
        return studentsIds.stream()
                .map(id -> (UserTCC) causalConsistencyService.addAggregateCausalSnapshot(id, unitOfWork))
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<UserDto> getTeachers(UnitOfWork unitOfWork) {
        Set<Integer> teacherIds = userTCCRepository.findAll().stream()
                .filter(u -> u.getRole().equals(Role.TEACHER))
                .map(User::getAggregateId)
                .collect(Collectors.toSet());
        return teacherIds.stream()
                .map(id -> (UserTCC) causalConsistencyService.addAggregateCausalSnapshot(id, unitOfWork))
                .map(UserDto::new)
                .collect(Collectors.toList());
    }
}
