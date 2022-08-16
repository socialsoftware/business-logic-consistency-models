package pt.ulisboa.tecnico.socialsoftware.blcm.topic;

import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;

import javax.transaction.Transactional;

@Service
public class TopicService {
    @Transactional
    public TopicDto getTopicByAggregateId(Integer topicAggregateId, UnitOfWork unitOfWork) {
        return new TopicDto();
    }
}
