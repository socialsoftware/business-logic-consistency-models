package pt.ulisboa.tecnico.socialsoftware.blcm.topic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.UpdateTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.TopicCourse;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.DELETED;

@Service
public class TopicService {

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private EventRepository eventRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TopicDto getCausalTopicRemote(Integer topicAggregateId, UnitOfWork unitOfWork) {
        return new TopicDto(getCausalTopicLocal(topicAggregateId, unitOfWork));
    }



    // intended for requests from local functionalities
    public Topic getCausalTopicLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        Topic topic = topicRepository.findCausal(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(ErrorMessage.TOPIC_NOT_FOUND, aggregateId));

        if(topic.getState() == DELETED) {
            throw new TutorException(ErrorMessage.TOPIC_DELETED, topic.getAggregateId());
        }

        List<Event> allEvents = eventRepository.findAll();
        unitOfWork.addToCausalSnapshot(topic, allEvents);
        return topic;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TopicDto createTopic(TopicDto topicDto, TopicCourse course, UnitOfWork unitOfWorkWorkService) {
        Topic topic = new Topic(aggregateIdGeneratorService.getNewAggregateId(),
                topicDto.getName(), course);
        unitOfWorkWorkService.registerChanged(topic);
        return new TopicDto(topic);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<TopicDto> findCourseByTopicId(Integer courseAggregateId, UnitOfWork unitOfWork) {
        return topicRepository.findAll().stream()
                .filter(t -> courseAggregateId == t.getCourse().getAggregateId())
                .map(Topic::getAggregateId)
                .distinct()
                .map(aggregateId -> getCausalTopicLocal(aggregateId, unitOfWork))
                .map(TopicDto::new)
                .collect(Collectors.toList());

    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateTopic(TopicDto topicDto, UnitOfWork unitOfWork) {
        Topic oldTopic = getCausalTopicLocal(topicDto.getAggregateId(), unitOfWork);
        Topic newTopic = new Topic(oldTopic);
        newTopic.setName(topicDto.getName());
        unitOfWork.registerChanged(newTopic);
        unitOfWork.addEvent(new UpdateTopicEvent(newTopic));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteTopic(Integer topicAggregateId, UnitOfWork unitOfWork) {
        Topic oldTopic = getCausalTopicLocal(topicAggregateId, unitOfWork);
        Topic newTopic = new Topic(oldTopic);
        newTopic.remove();
        unitOfWork.registerChanged(newTopic);
        unitOfWork.addEvent(new DeleteTopicEvent(newTopic));
    }
}
