package pt.ulisboa.tecnico.socialsoftware.blcm.topic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.dto.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.service.CourseService;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.TopicCourse;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.service.TopicService;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWorkService;

import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

@Service
public class TopicFunctionalities {

    @Autowired
    private TopicService topicService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public List<TopicDto> findTopicsByCourseAggregateId(Integer courseAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return topicService.findCourseByTopicId(courseAggregateId, unitOfWork);
    }

    public TopicDto createTopic(Integer courseAggregateId, TopicDto topicDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        checkInput(topicDto);
        CourseDto courseDto = courseService.getCausalCourseRemote(courseAggregateId, unitOfWork);
        TopicCourse course = new TopicCourse(courseDto);
        TopicDto topicDto1 = topicService.createTopic(topicDto, course, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
        return topicDto1;
    }

    public void updateTopic(TopicDto topicDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        checkInput(topicDto);
        topicService.updateTopic(topicDto, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void deleteTopic(Integer topicAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        topicService.deleteTopic(topicAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    private void checkInput(TopicDto topicDto) {
        if (topicDto.getName() == null) {
            throw new TutorException(TOPIC_MISSING_NAME);
        }
    }
}
