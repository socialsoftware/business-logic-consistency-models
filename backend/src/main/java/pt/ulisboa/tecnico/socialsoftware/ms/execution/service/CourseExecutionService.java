package pt.ulisboa.tecnico.socialsoftware.ms.execution.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.course.service.CourseService;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.domain.CourseExecutionStudent;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.AnonymizeStudentEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.UnerollStudentFromCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.UpdateStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.repository.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.ms.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.ms.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.domain.CourseExecutionCourse;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.dto.CourseExecutionDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.ms.exception.ErrorMessage.*;

@Service
public class CourseExecutionService {
    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CourseService courseService;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseExecutionDto getCausalCourseExecutionRemote(Integer executionAggregateId, UnitOfWork unitOfWorkWorkService) {
        return new CourseExecutionDto(getCausalCourseExecutionLocal(executionAggregateId, unitOfWorkWorkService));
    }

    // intended for requests from local functionalities
    public CourseExecution getCausalCourseExecutionLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        CourseExecution execution = courseExecutionRepository.findCausal(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND, aggregateId));

        if (execution.getState() == Aggregate.AggregateState.DELETED) {
            throw new TutorException(COURSE_EXECUTION_DELETED, execution.getAggregateId());
        }

        List<Event> allEvents = eventRepository.findAll();
        unitOfWork.addToCausalSnapshot(execution, allEvents);
        return execution;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseExecutionDto createCourseExecution(CourseExecutionDto courseExecutionDto, UnitOfWork unitOfWork) {
        CourseExecutionCourse courseExecutionCourse = new CourseExecutionCourse(courseService.getAndOrCreateCourseRemote(courseExecutionDto, unitOfWork));

        CourseExecution courseExecution = new CourseExecution(aggregateIdGeneratorService.getNewAggregateId(), courseExecutionDto, courseExecutionCourse);

        unitOfWork.registerChanged(courseExecution);
        return new CourseExecutionDto(courseExecution);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<CourseExecutionDto> getAllCausalCourseExecutions(UnitOfWork unitOfWork) {
        return courseExecutionRepository.findAggregateIdsOfAllNonDeleted().stream()
                .map(id -> getCausalCourseExecutionLocal(id, unitOfWork))
                .map(CourseExecutionDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeCourseExecution(Integer executionAggregateId, UnitOfWork unitOfWork) {

        CourseExecution oldCourseExecution = getCausalCourseExecutionLocal(executionAggregateId, unitOfWork);
        CourseExecution newCourseExecution = new CourseExecution(oldCourseExecution);

        /*
            REMOVE_COURSE_IS_VALID
         */
        Integer numberOfExecutionsOfCourse = Math.toIntExact(getAllCausalCourseExecutions(unitOfWork).stream()
                .filter(ce -> ce.getCourseAggregateId() == newCourseExecution.getExecutionCourse().getCourseAggregateId())
                .count());
        if (numberOfExecutionsOfCourse == 1) {
            throw new TutorException(CANNOT_DELETE_COURSE_EXECUTION);
        }

        newCourseExecution.remove();
        unitOfWork.registerChanged(newCourseExecution);
        unitOfWork.addEvent(new RemoveCourseExecutionEvent(newCourseExecution.getAggregateId()));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void enrollStudent(Integer courseExecutionAggregateId, UserDto userDto, UnitOfWork unitOfWork) {
        CourseExecution oldCourseExecution = getCausalCourseExecutionLocal(courseExecutionAggregateId, unitOfWork);

        CourseExecutionStudent courseExecutionStudent = new CourseExecutionStudent(userDto);
        if (!courseExecutionStudent.isActive()){
            throw new TutorException(ErrorMessage.INACTIVE_USER, courseExecutionStudent.getUserAggregateId());
        }

        CourseExecution newCourseExecution = new CourseExecution(oldCourseExecution);
        newCourseExecution.addStudent(courseExecutionStudent);

        unitOfWork.registerChanged(newCourseExecution);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Set<CourseExecutionDto> getCourseExecutionsByUser(Integer userAggregateId, UnitOfWork unitOfWork) {
        return courseExecutionRepository.findAll().stream()
                .map(CourseExecution::getAggregateId)
                .map(aggregateId -> getCausalCourseExecutionLocal(aggregateId, unitOfWork))
                .filter(ce -> ce.hasStudent(userAggregateId))
                .map(ce -> new CourseExecutionDto(ce))
                .collect(Collectors.toSet());

    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeStudentFromCourseExecution(Integer courseExecutionAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        CourseExecution oldCourseExecution = getCausalCourseExecutionLocal(courseExecutionAggregateId, unitOfWork);
        CourseExecution newCourseExecution = new CourseExecution(oldCourseExecution);
        newCourseExecution.removeStudent(userAggregateId);
        unitOfWork.registerChanged(newCourseExecution);
        unitOfWork.addEvent(new UnerollStudentFromCourseExecutionEvent(courseExecutionAggregateId, userAggregateId));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserDto getStudentByExecutionIdAndUserId(Integer executionAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        CourseExecution courseExecution = getCausalCourseExecutionLocal(executionAggregateId, unitOfWork);
        if (!courseExecution.hasStudent(userAggregateId)) {
            throw new TutorException(COURSE_EXECUTION_STUDENT_NOT_FOUND, userAggregateId, executionAggregateId);
        }
        return courseExecution.findStudent(userAggregateId).buildDto();
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void anonymizeStudent(Integer executionAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        CourseExecution oldExecution = getCausalCourseExecutionLocal(executionAggregateId, unitOfWork);
        if(!oldExecution.hasStudent(userAggregateId)) {
            throw new TutorException(COURSE_EXECUTION_STUDENT_NOT_FOUND, userAggregateId, executionAggregateId);
        }
        CourseExecution newExecution = new CourseExecution(oldExecution);
        newExecution.findStudent(userAggregateId).anonymize();
        unitOfWork.registerChanged(newExecution);
        unitOfWork.addEvent(new AnonymizeStudentEvent(executionAggregateId, "ANONYMOUS", "ANONYMOUS", userAggregateId));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateExecutionStudentName(Integer executionAggregateId, Integer userAggregateId, String name, UnitOfWork unitOfWork) {
        CourseExecution oldExecution = getCausalCourseExecutionLocal(executionAggregateId, unitOfWork);
        if (!oldExecution.hasStudent(userAggregateId)) {
            throw new TutorException(COURSE_EXECUTION_STUDENT_NOT_FOUND, userAggregateId, executionAggregateId);
        }
        CourseExecution newExecution = new CourseExecution(oldExecution);
        newExecution.findStudent(userAggregateId).setName(name);
        unitOfWork.registerChanged(newExecution);
        unitOfWork.addEvent(new UpdateStudentNameEvent(executionAggregateId, userAggregateId, name));
    }

    // EVENT DETECTION SUBSCRIPTIONS
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Set<EventSubscription> getEventSubscriptions(Integer aggregateId, Integer versionId, String eventType) {
        CourseExecution courseExecution = courseExecutionRepository.findVersionByAggregateIdAndVersionId(aggregateId, versionId).get();
        return courseExecution.getEventSubscriptionsByEventType(eventType);
    }

    /************************************************ EVENT PROCESSING ************************************************/


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseExecution removeUser(Integer executionAggregateId, Integer userAggregateId, Integer aggregateEventVersion, UnitOfWork unitOfWork) {
        CourseExecution oldExecution = getCausalCourseExecutionLocal(executionAggregateId, unitOfWork);
        CourseExecution newExecution = new CourseExecution(oldExecution);
        newExecution.findStudent(userAggregateId).setState(Aggregate.AggregateState.INACTIVE);
        unitOfWork.registerChanged(newExecution);
        unitOfWork.addEvent(new UnerollStudentFromCourseExecutionEvent(executionAggregateId, userAggregateId));
        return newExecution;
    }
}
