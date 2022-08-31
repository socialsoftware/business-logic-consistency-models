package pt.ulisboa.tecnico.socialsoftware.blcm.topic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.Course;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.UpdateTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.TopicCourse;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;

import javax.transaction.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.COURSE_EXECUTION_DELETED;

@Service
public class TopicService {

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private TopicRepository topicRepository;

    @Transactional
    public TopicDto getCausalTopicRemote(Integer topicAggregateId, UnitOfWork unitOfWorkWorkService) {
        return new TopicDto(getCausalTopicLocal(topicAggregateId, unitOfWorkWorkService));
    }



    // intended for requests from local functionalities
    @Transactional
    public Topic getCausalTopicLocal(Integer aggregateId, UnitOfWork unitOfWorkWorkService) {
        Topic topic = topicRepository.findByAggregateIdAndVersion(aggregateId, unitOfWorkWorkService.getVersion())
                .orElseThrow(() -> new TutorException(ErrorMessage.TOPIC_NOT_FOUND, aggregateId));

        if(topic.getState().equals(DELETED)) {
            throw new TutorException(ErrorMessage.TOPIC_DELETED, topic.getAggregateId());
        }

        topic.checkDependencies(unitOfWorkWorkService);
        return topic;
    }

    @Transactional
    public TopicDto createTopic(TopicDto topicDto, TopicCourse course, UnitOfWork unitOfWorkWorkService) {
        Topic topic = new Topic(aggregateIdGeneratorService.getNewAggregateId(),
                unitOfWorkWorkService.getVersion(), topicDto.getName(), course);
        topicRepository.save(topic);
        unitOfWorkWorkService.addUpdatedObject(topic, "Topic");
        return new TopicDto(topic);
    }

    @Transactional
    public List<TopicDto> findCourseByTopicId(Integer courseAggregateId, UnitOfWork unitOfWork) {
        return topicRepository.findAll().stream()
                .filter(t -> courseAggregateId == t.getCourse().getAggregateId())
                .map(Topic::getAggregateId)
                .distinct()
                .map(aggregateId -> getCausalTopicLocal(aggregateId, unitOfWork))
                .map(TopicDto::new)
                .collect(Collectors.toList());

    }

    @Transactional
    public void updateTopic(TopicDto topicDto, UnitOfWork unitOfWork) {
        Topic oldTopic = getCausalTopicLocal(topicDto.getAggregateId(), unitOfWork);
        Topic newTopic = new Topic(oldTopic);
        newTopic.setName(topicDto.getName());
        topicRepository.save(newTopic);
        unitOfWork.addUpdatedObject(newTopic, "Topic");
        unitOfWork.addEvent(new UpdateTopicEvent(newTopic));
    }

    public void deleteTopic(Integer topicAggregateId, UnitOfWork unitOfWork) {
        Topic oldTopic = getCausalTopicLocal(topicAggregateId, unitOfWork);
        Topic newTopic = new Topic(oldTopic);
        newTopic.setState(DELETED);
        topicRepository.save(newTopic);
        unitOfWork.addUpdatedObject(newTopic, "Topic");
        unitOfWork.addEvent(new DeleteTopicEvent(newTopic));
    }
}
