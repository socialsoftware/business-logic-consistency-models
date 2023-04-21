package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.event.publish.QuizAnswerQuestionAnswerEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.AnonymizeStudentEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.UnerollStudentFromCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.UpdateStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.event.publish.InvalidateQuizEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.event.publish.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.event.publish.UpdateTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service.TournamentService;

@Service
public class TournamentEventProcessing {
    private static final Logger logger = LoggerFactory.getLogger(TournamentEventProcessing.class);

    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public void processAnonymizeStudentEvent(Integer aggregateId, AnonymizeStudentEvent anonymizeEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("Processing anonymize a user for course execution {} event for tournament {}", anonymizeEvent.getPublisherAggregateId(), aggregateId);
        tournamentService.anonymizeUser(aggregateId, anonymizeEvent.getPublisherAggregateId(), anonymizeEvent.getStudentAggregateId(), anonymizeEvent.getName(), anonymizeEvent.getUsername(), anonymizeEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processRemoveCourseExecution (Integer aggregateId, RemoveCourseExecutionEvent removeCourseExecutionEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("Processing remove execution {} event for tournament {}", removeCourseExecutionEvent.getPublisherAggregateId(), aggregateId);
        tournamentService.removeCourseExecution(aggregateId, removeCourseExecutionEvent.getPublisherAggregateId(), removeCourseExecutionEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processUpdateTopic (Integer aggregateId, UpdateTopicEvent updateTopicEvent){
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("Processing update topic {} event for tournament {}", updateTopicEvent.getPublisherAggregateId(), aggregateId);
        tournamentService.updateTopic(aggregateId, updateTopicEvent.getPublisherAggregateId(), updateTopicEvent.getTopicName(), updateTopicEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processDeleteTopic (Integer aggregateId, DeleteTopicEvent deleteTopicEvent){
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("Processing delete topic {} event for tournament {}", deleteTopicEvent.getPublisherAggregateId(), aggregateId);
        tournamentService.removeTopic(aggregateId, deleteTopicEvent.getPublisherAggregateId(), deleteTopicEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processAnswerQuestion(Integer aggregateId, QuizAnswerQuestionAnswerEvent quizAnswerQuestionAnswerEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("Processing answer question {} event for tournament {}", quizAnswerQuestionAnswerEvent.getPublisherAggregateId(), aggregateId);
        tournamentService.updateParticipantAnswer(aggregateId, quizAnswerQuestionAnswerEvent.getStudentAggregateId(), quizAnswerQuestionAnswerEvent.getPublisherAggregateId(), quizAnswerQuestionAnswerEvent.getQuestionAggregateId(), quizAnswerQuestionAnswerEvent.isCorrect(), quizAnswerQuestionAnswerEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processUnenrollStudent(Integer aggregateId, UnerollStudentFromCourseExecutionEvent unerollStudentFromCourseExecutionEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("Processing unenroll student {} event for tournament {}", unerollStudentFromCourseExecutionEvent.getPublisherAggregateId(), aggregateId);
        tournamentService.removeUser(aggregateId, unerollStudentFromCourseExecutionEvent.getPublisherAggregateId(), unerollStudentFromCourseExecutionEvent.getStudentAggregateId() ,unerollStudentFromCourseExecutionEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processInvalidateQuizEvent(Integer aggregateId, InvalidateQuizEvent invalidateQuizEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("Processing invalidate quiz {} event for tournament {}", invalidateQuizEvent.getPublisherAggregateId(), aggregateId);
        tournamentService.invalidateQuiz(aggregateId, invalidateQuizEvent.getPublisherAggregateId(), invalidateQuizEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processUpdateExecutionStudentNameEvent(Integer subscriberAggregateId, UpdateStudentNameEvent updateStudentNameEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("START PROCESS EVENT: update execution student name with version {}", unitOfWork.getVersion());

        tournamentService.updateUserName(subscriberAggregateId, updateStudentNameEvent.getPublisherAggregateId(), updateStudentNameEvent.getPublisherAggregateVersion(), updateStudentNameEvent.getStudentAggregateId(), updateStudentNameEvent.getUpdatedName(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);

        logger.info("END PROCESS EVENT: update execution student name with version {}", unitOfWork.getVersion());

    }
}
