package pt.ulisboa.tecnico.socialsoftware.blcm.execution;

import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;

import javax.transaction.Transactional;

@Service
public class CourseExecutionService {
    @Transactional
    public CourseExecutionDto getCourseExecutionById(Integer executionId, UnitOfWork unitOfWork) {
        return new CourseExecutionDto();
    }
}
