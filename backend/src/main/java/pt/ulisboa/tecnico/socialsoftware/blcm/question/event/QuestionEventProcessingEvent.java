package pt.ulisboa.tecnico.socialsoftware.blcm.question.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.service.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.event.publish.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.event.publish.UpdateTopicEvent;


@Service
public class QuestionEventProcessingEvent {
    @Autowired
    private QuestionService questionService;
    @Autowired
    private UnitOfWorkService unitOfWorkService;


    /************************************************ EVENT PROCESSING ************************************************/

    public void processUpdateTopic(Integer aggregateId, UpdateTopicEvent updateTopicEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        questionService.updateTopic(aggregateId, updateTopicEvent.getPublisherAggregateId(), updateTopicEvent.getTopicName(), updateTopicEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processRemoveTopic(Integer aggregateId, DeleteTopicEvent deleteTopicEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        questionService.removeTopic(aggregateId, deleteTopicEvent.getPublisherAggregateId(), deleteTopicEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
}