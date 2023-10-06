package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.causal.causalUnityOfWork.CausalUnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.aggregate.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.event.publish.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.event.publish.UpdateTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.aggregate.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.causal.domain.CausalTopic;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.aggregate.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.aggregate.Topic;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.aggregate.TopicCourse;
import pt.ulisboa.tecnico.socialsoftware.ms.causal.causalUnityOfWork.CausalUnitOfWork;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TopicService {

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private CausalUnitOfWorkService unitOfWorkService;

    @Autowired
    private TopicRepository topicRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TopicDto getTopicById(Integer topicAggregateId, CausalUnitOfWork unitOfWork) {
        return new TopicDto((Topic) unitOfWorkService.aggregateLoadAndRegisterRead(topicAggregateId, unitOfWork));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TopicDto createTopic(TopicDto topicDto, TopicCourse course, CausalUnitOfWork unitOfWorkWorkService) {
        Topic topic = new CausalTopic(aggregateIdGeneratorService.getNewAggregateId(),
                topicDto.getName(), course);
        unitOfWorkWorkService.registerChanged(topic);
        return new TopicDto(topic);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<TopicDto> findTopicsByCourseId(Integer courseAggregateId, CausalUnitOfWork unitOfWork) {
        return topicRepository.findAll().stream()
                .filter(t -> courseAggregateId == t.getTopicCourse().getCourseAggregateId())
                .map(Topic::getAggregateId)
                .distinct()
                .map(aggregateId -> (Topic) unitOfWorkService.aggregateLoadAndRegisterRead(aggregateId, unitOfWork))
                .map(TopicDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateTopic(TopicDto topicDto, CausalUnitOfWork unitOfWork) {
        Topic oldTopic = (Topic) unitOfWorkService.aggregateLoadAndRegisterRead(topicDto.getAggregateId(), unitOfWork);
        Topic newTopic = new CausalTopic((CausalTopic) oldTopic);
        newTopic.setName(topicDto.getName());
        unitOfWork.registerChanged(newTopic);
        unitOfWork.addEvent(new UpdateTopicEvent(newTopic.getAggregateId(), newTopic.getName()));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteTopic(Integer topicAggregateId, CausalUnitOfWork unitOfWork) {
        Topic oldTopic = (Topic) unitOfWorkService.aggregateLoadAndRegisterRead(topicAggregateId, unitOfWork);
        Topic newTopic = new CausalTopic((CausalTopic) oldTopic);
        newTopic.remove();
        unitOfWork.registerChanged(newTopic);
        unitOfWork.addEvent(new DeleteTopicEvent(newTopic.getAggregateId()));
    }
}
