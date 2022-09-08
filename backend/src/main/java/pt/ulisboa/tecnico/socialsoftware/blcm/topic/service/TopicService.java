package pt.ulisboa.tecnico.socialsoftware.blcm.topic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.service.AggregateIdGeneratorService;
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
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.DELETED;

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
    public Topic getCausalTopicLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        Topic topic = topicRepository.findByAggregateIdAndVersion(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(ErrorMessage.TOPIC_NOT_FOUND, aggregateId));

        if(topic.getState().equals(DELETED)) {
            throw new TutorException(ErrorMessage.TOPIC_DELETED, topic.getAggregateId());
        }

        topic.checkDependencies(unitOfWork);
        unitOfWork.addCurrentReadDependencies(topic.getDependenciesMap());
        return topic;
    }

    @Transactional
    public TopicDto createTopic(TopicDto topicDto, TopicCourse course, UnitOfWork unitOfWorkWorkService) {
        Topic topic = new Topic(aggregateIdGeneratorService.getNewAggregateId(),
                unitOfWorkWorkService.getVersion(), topicDto.getName(), course);
        unitOfWorkWorkService.addUpdatedObject(topic);
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
        unitOfWork.addUpdatedObject(newTopic);
        unitOfWork.addEvent(new UpdateTopicEvent(newTopic));
    }

    public void deleteTopic(Integer topicAggregateId, UnitOfWork unitOfWork) {
        Topic oldTopic = getCausalTopicLocal(topicAggregateId, unitOfWork);
        Topic newTopic = new Topic(oldTopic);
        newTopic.remove();
        unitOfWork.addUpdatedObject(newTopic);
        unitOfWork.addEvent(new DeleteTopicEvent(newTopic));
    }
}
