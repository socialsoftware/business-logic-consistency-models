package pt.ulisboa.tecnico.socialsoftware.blcm.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.AnonymizeUserEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import javax.transaction.Transactional;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /*simple user creation*/
    @Transactional
    public UserDto createUser(UserDto userDto) {
        User user = new User(userDto);
        return new UserDto(user);
    }
    @Transactional
    public UserDto getUserById(Integer userId, UnitOfWork unitOfWork) {
        return new UserDto();
    }

    @Transactional
    public void anonymizeCourseExecutionUsers(Integer executionAggregateId, UnitOfWork unitOfWork) {
        Set<User> executionsUsers = userRepository.findByExecutionAggregateIdAndVersion(executionAggregateId, unitOfWork.getVersion());
        executionsUsers.forEach(oldUser -> {
            User newUser = new User(oldUser);
            newUser.anonymize();
            unitOfWork.addUpdatedObject(newUser);
            unitOfWork.addEvent(new AnonymizeUserEvent(newUser.getAggregateId()));
        });
    }

}
