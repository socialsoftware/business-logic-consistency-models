package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.service.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.event.publish.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.event.publish.UpdateTopicEvent;


@Service
public class QuestionEventProcessing {
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
