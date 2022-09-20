package pt.ulisboa.tecnico.socialsoftware.blcm.user.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.DomainEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.repository.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.service.UserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.REMOVE_COURSE_EXECUTION;

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
        UserProcessedEvents userProcessedEvents = userProcessedEventsRepository.findAll().stream()
                .filter(upe -> REMOVE_COURSE_EXECUTION.equals(upe.getEventType()))
                .findFirst()
                .orElse(new UserProcessedEvents(REMOVE_COURSE_EXECUTION));

        Set<DomainEvent> events = eventRepository.findAll().stream()
                .filter(e -> e.getType().equals(REMOVE_COURSE_EXECUTION))
                .filter(e -> !(userProcessedEvents.containsEvent(e.getId())))
                .collect(Collectors.toSet());

        for(DomainEvent e : events) {
            handleRemoveCourseExecution((RemoveCourseExecutionEvent) e);
        }

        Set<Integer> processedEventsIds = events.stream()
                .map(DomainEvent::getId)
                .collect(Collectors.toSet());
        userProcessedEvents.addProcessedEventsIds(processedEventsIds);
        userProcessedEventsRepository.save(userProcessedEvents);

    }

    private void handleRemoveCourseExecution(RemoveCourseExecutionEvent e) {
        RemoveCourseExecutionEvent removeCourseExecutionEvent = e;
        List<Integer> executionsAggregateIds = List.of(removeCourseExecutionEvent.getCourseExecutionId());
        Set<Integer> usersAggregateIds = userRepository.findAllNonDeleted().stream()
                .map(User::getAggregateId)
                .collect(Collectors.toSet());
        for(Integer userAggregateId : usersAggregateIds) {
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            userService.removeCourseExecutionsFromUser(userAggregateId, executionsAggregateIds, unitOfWork);
            unitOfWorkService.commit(unitOfWork);
        }
    }

}
