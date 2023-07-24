package pt.ulisboa.tecnico.socialsoftware.ms.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.user.domain.Role;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.ms.user.event.publish.RemoveUserEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.ms.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.ms.user.repository.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.ms.user.dto.UserDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.ms.exception.ErrorMessage.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private EventRepository eventRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserDto getCausalUserRemote(Integer aggregateId, UnitOfWork unitOfWork) {
        return new UserDto(getCausalUserLocal(aggregateId, unitOfWork));
    }

    // intended for requests from local functionalities
    public User getCausalUserLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        User user = userRepository.findCausal(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, aggregateId));

        if (user.getState() == Aggregate.AggregateState.DELETED) {
            throw new TutorException(ErrorMessage.USER_DELETED, user.getAggregateId());
        }

        List<Event> allEvents = eventRepository.findAll();
        unitOfWork.addToCausalSnapshot(user, allEvents);
        return user;
    }

    /*simple user creation*/
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserDto createUser(UserDto userDto, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        User user = new User(aggregateId, userDto);
        unitOfWork.registerChanged(user);
        return new UserDto(user);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void activateUser(Integer userAggregateId, UnitOfWork unitOfWork) {
        User oldUser = getCausalUserLocal(userAggregateId, unitOfWork);
        if (oldUser.isActive()) {
            throw new TutorException(USER_ACTIVE);
        }
        User newUser = new User(oldUser);
        newUser.setActive(true);
        unitOfWork.registerChanged(newUser);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteUser(Integer userAggregateId, UnitOfWork unitOfWork) {
        User oldUser = getCausalUserLocal(userAggregateId, unitOfWork);
        User newUser = new User(oldUser);
        newUser.remove();
        unitOfWork.registerChanged(newUser);
        unitOfWork.addEvent(new RemoveUserEvent(newUser.getAggregateId()));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
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

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
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
