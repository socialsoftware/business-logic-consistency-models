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

import javax.transaction.Transactional;
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

    @Transactional
    @Scheduled(cron = "*/10 * * * * *")
    public void detectRemoveCourseExecutionEvents() {
        UserProcessedEvents lastProcessedEvent = userProcessedEventsRepository.findAll().stream()
                .findFirst()
                .orElse(new UserProcessedEvents(0));

        Set<DomainEvent> events = eventRepository.findAll()
                .stream()
                .filter(e -> e.getId() > lastProcessedEvent.getLastProcessedEventId())
                .map(e -> (RemoveCourseExecutionEvent) e)
                .collect(Collectors.toSet());

        for(DomainEvent e : events) {
            switch (e.getType()) {
                case REMOVE_COURSE_EXECUTION:
            }
            handleRemoveCourseExecution((RemoveCourseExecutionEvent) e);
        }

        Integer newLastProcessedId = events.stream().map(DomainEvent::getId).max(Integer::compareTo).orElse(lastProcessedEvent.getLastProcessedEventId());
        lastProcessedEvent.setLastProcessedEventId(newLastProcessedId);
        userProcessedEventsRepository.save(lastProcessedEvent);

    }

    private void handleRemoveCourseExecution(RemoveCourseExecutionEvent e) {
        RemoveCourseExecutionEvent removeCourseExecutionEvent = e;
        List<Integer> executionsAggregateIds = List.of(removeCourseExecutionEvent.getCourseExecutionId());
        Set<Integer> usersAggregateIds = userRepository.findAllNonDeleted().stream()
                .map(User::getAggregateId)
                .collect(Collectors.toSet());
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        for(Integer userAggregateId : usersAggregateIds) {
            userService.removeCourseExecutionsFromUser(userAggregateId, executionsAggregateIds, unitOfWork);
        }
        unitOfWorkService.commit(unitOfWork);
    }

}
