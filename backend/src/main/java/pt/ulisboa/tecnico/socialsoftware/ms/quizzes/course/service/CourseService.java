package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.domain.Course;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.tcc.CourseTCC;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.repository.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.dto.CourseDto;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CourseService {
    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;
    @Autowired
    private UnitOfWorkService unitOfWorkService;
    @Autowired
    private CourseRepository courseRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseDto getCourseById(Integer aggregateId, UnitOfWork unitOfWorkWorkService) {
        return new CourseDto((Course) unitOfWorkService.aggregateLoadAndRegisterRead(aggregateId, unitOfWorkWorkService));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseExecutionDto getAndOrCreateCourseRemote(CourseExecutionDto courseExecutionDto, UnitOfWork unitOfWork) {
        Course course = getCourseByName(courseExecutionDto.getName(), unitOfWork);
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

    private Course getCourseByName(String courseName, UnitOfWork unitOfWork) {
        return courseRepository.findCourseIdByName(courseName)
                .map(id -> (Course) unitOfWorkService.aggregateLoadAndRegisterRead(id, unitOfWork))
                .orElse(null);
    }

}
