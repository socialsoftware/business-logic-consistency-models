package pt.ulisboa.tecnico.socialsoftware.blcm.user.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.repository.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.service.UserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.event.EventType.REMOVE_COURSE_EXECUTION;

@Component
public class UserEventDetection {

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProcessedEventsRepository userProcessedEventsRepository;


    @Scheduled(cron = "*/10 * * * * *")
    public void detectRemoveCourseExecutionEvents() {
        UserProcessedEvents lastProcessedEvent = userProcessedEventsRepository.findAll().stream()
                .filter(pe -> pe.getEventType().equals(REMOVE_COURSE_EXECUTION))
                .findFirst()
                .orElse(new UserProcessedEvents(0, REMOVE_COURSE_EXECUTION));

        Set<RemoveCourseExecutionEvent> events = eventRepository.getEvents(REMOVE_COURSE_EXECUTION, lastProcessedEvent.getLastProcessed())
                .stream()
                .map(e -> (RemoveCourseExecutionEvent) e)
                .collect(Collectors.toSet());

        List<Integer> executionsAggregateIds = events.stream()
                .map(RemoveCourseExecutionEvent::getCourseExecutionId)
                .distinct()
                .collect(Collectors.toList());

        Set<Integer> usersAggregateIds = userRepository.findAllNonDeleted().stream()
                .map(User::getAggregateId)
                .collect(Collectors.toSet());


        if(!events.isEmpty()) {
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            for(Integer userAggregateId : usersAggregateIds) {
                userService.removeCourseExecutionsFromUser(userAggregateId, executionsAggregateIds, unitOfWork);
            }
            unitOfWorkService.commit(unitOfWork);
        }


        Integer newLastProcessedId = events.stream().map(RemoveCourseExecutionEvent::getId).max(Integer::compareTo).orElse(lastProcessedEvent.getLastProcessed());

        lastProcessedEvent.setLastProcessed(newLastProcessedId);
        userProcessedEventsRepository.save(lastProcessedEvent);

    }

}
