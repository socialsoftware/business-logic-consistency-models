package pt.ulisboa.tecnico.socialsoftware.blcm.course.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.repository.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.repository.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.domain.Course;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.dto.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;

import java.sql.SQLException;
import java.util.List;

@Service
public class CourseService {

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EventRepository eventRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseDto getCausalCourseRemote(Integer aggregateId, UnitOfWork unitOfWorkWorkService) {
        return new CourseDto(getCausalCourseLocal(aggregateId, unitOfWorkWorkService));
    }

    // intended for requests from local functionalities

    public Course getCausalCourseLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        Course course = courseRepository.findCausal(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(ErrorMessage.COURSE_NOT_FOUND, aggregateId));

        if(course.getState() == Aggregate.AggregateState.DELETED) {
            throw new TutorException(ErrorMessage.COURSE_DELETED, course.getAggregateId());
        }

        List<Event> allEvents = eventRepository.findAll();
        unitOfWork.addToCausalSnapshot(course, allEvents);
        return course;
    }



    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseExecutionDto getAndOrCreateCourseRemote(CourseExecutionDto courseExecutionDto, UnitOfWork unitOfWork) {
        Course course = getCausalCourseLocalByName(courseExecutionDto.getName(), unitOfWork);
        if(course == null) {
            Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
            course = new Course(aggregateId, courseExecutionDto);
            unitOfWork.registerChanged(course);
        }
        courseExecutionDto.setCourseAggregateId(course.getAggregateId());
        courseExecutionDto.setName(course.getName());
        courseExecutionDto.setType(course.getType().toString());
        courseExecutionDto.setCourseVersion(course.getVersion());
        return courseExecutionDto;
    }

    private Course getCausalCourseLocalByName(String courseName, UnitOfWork unitOfWork) {
        Course course = courseRepository.findCausalByName(courseName, unitOfWork.getVersion())
                .orElse(null);
        if(course != null) {
            List<Event> allEvents = eventRepository.findAll();
            unitOfWork.addToCausalSnapshot(course, allEvents);
        }
        return course;
    }


}
