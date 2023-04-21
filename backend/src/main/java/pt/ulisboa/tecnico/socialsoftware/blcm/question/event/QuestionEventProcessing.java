package pt.ulisboa.tecnico.socialsoftware.blcm.question.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.service.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.event.publish.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.event.publish.UpdateTopicEvent;


@Service
public class QuestionEventProcessing {
    private static final Logger logger = LoggerFactory.getLogger(QuestionEventProcessing.class);

    @Autowired
    private QuestionService questionService;
    @Autowired
    private UnitOfWorkService unitOfWorkService;


    /************************************************ EVENT PROCESSING ************************************************/

    public void processUpdateTopic(Integer aggregateId, UpdateTopicEvent updateTopicEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("Processing update topic {} event for question {}", updateTopicEvent.getPublisherAggregateId(), aggregateId);
        questionService.updateTopic(aggregateId, updateTopicEvent.getPublisherAggregateId(), updateTopicEvent.getTopicName(), updateTopicEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processRemoveTopic(Integer aggregateId, DeleteTopicEvent deleteTopicEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("Processing delete topic {} event for question {}", deleteTopicEvent.getPublisherAggregateId(), aggregateId);
        questionService.removeTopic(aggregateId, deleteTopicEvent.getPublisherAggregateId(), deleteTopicEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
}
