package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.causal.coordination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.topic.domain.TopicCourse;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.topic.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.topic.service.TopicService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.course.dto.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.course.service.CourseService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.ms.causal.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.causal.unityOfWork.UnitOfWorkService;

import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.exception.ErrorMessage.*;

@Service
public class TopicFunctionalities {
    @Autowired
    private TopicService topicService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public List<TopicDto> findTopicsByCourseAggregateId(Integer courseAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        return topicService.findTopicsByCourseId(courseAggregateId, unitOfWork);
    }

    public TopicDto createTopic(Integer courseAggregateId, TopicDto topicDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        checkInput(topicDto);
        CourseDto courseDto = courseService.getCourseById(courseAggregateId, unitOfWork);
        TopicCourse course = new TopicCourse(courseDto);
        TopicDto topicDto1 = topicService.createTopic(topicDto, course, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
        return topicDto1;
    }

    public void updateTopic(TopicDto topicDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        checkInput(topicDto);
        topicService.updateTopic(topicDto, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void deleteTopic(Integer topicAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        topicService.deleteTopic(topicAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    private void checkInput(TopicDto topicDto) {
        if (topicDto.getName() == null) {
            throw new TutorException(TOPIC_MISSING_NAME);
        }
    }
}
