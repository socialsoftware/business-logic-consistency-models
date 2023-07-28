package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.tcc.CourseTCC;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.service.CausalConsistencyService;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.tcc.CourseTCCRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.dto.CourseDto;

import java.sql.SQLException;
import java.util.List;

@Service
public class CourseService {

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private CausalConsistencyService causalConsistencyService;

    @Autowired
    private CourseTCCRepository courseTCCRepository;

    @Autowired
    private EventRepository eventRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseDto addCourseCausalSnapshot(Integer aggregateId, UnitOfWork unitOfWorkWorkService) {
        return new CourseDto((CourseTCC) causalConsistencyService.addAggregateCausalSnapshot(aggregateId, unitOfWorkWorkService));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseExecutionDto getAndOrCreateCourseRemote(CourseExecutionDto courseExecutionDto, UnitOfWork unitOfWork) {
        CourseTCC course = getCausalCourseLocalByName(courseExecutionDto.getName(), unitOfWork);
        if (course == null) {
            Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
            course = new CourseTCC(aggregateId, courseExecutionDto);
            unitOfWork.registerChanged(course);
        }
        courseExecutionDto.setCourseAggregateId(course.getAggregateId());
        courseExecutionDto.setName(course.getName());
        courseExecutionDto.setType(course.getType().toString());
        courseExecutionDto.setCourseVersion(course.getVersion());
        return courseExecutionDto;
    }

    private CourseTCC getCausalCourseLocalByName(String courseName, UnitOfWork unitOfWork) {
        CourseTCC course = courseTCCRepository.findCausalCourseByName(courseName, unitOfWork.getVersion())
                .orElse(null);
        if (course != null) {
            List<Event> allEvents = eventRepository.findAll();
            unitOfWork.addToCausalSnapshot(course, allEvents);
        }
        return course;
    }


}
