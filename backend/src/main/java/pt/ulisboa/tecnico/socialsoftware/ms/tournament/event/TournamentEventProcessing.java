package pt.ulisboa.tecnico.socialsoftware.ms.tournament.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.ms.answer.event.publish.QuizAnswerQuestionAnswerEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.AnonymizeStudentEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.UnerollStudentFromCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.UpdateStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quiz.event.publish.InvalidateQuizEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.topic.event.publish.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.topic.event.publish.UpdateTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.tournament.service.TournamentService;

@Service
public class TournamentEventProcessing {
    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public void processAnonymizeStudentEvent(Integer aggregateId, AnonymizeStudentEvent anonymizeEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        tournamentService.anonymizeUser(aggregateId, anonymizeEvent.getPublisherAggregateId(), anonymizeEvent.getStudentAggregateId(), anonymizeEvent.getName(), anonymizeEvent.getUsername(), anonymizeEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processRemoveCourseExecutionEvent(Integer aggregateId, RemoveCourseExecutionEvent removeCourseExecutionEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        tournamentService.removeCourseExecution(aggregateId, removeCourseExecutionEvent.getPublisherAggregateId(), removeCourseExecutionEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processUpdateTopicEvent(Integer aggregateId, UpdateTopicEvent updateTopicEvent){
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        tournamentService.updateTopic(aggregateId, updateTopicEvent.getPublisherAggregateId(), updateTopicEvent.getTopicName(), updateTopicEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processDeleteTopicEvent(Integer aggregateId, DeleteTopicEvent deleteTopicEvent){
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        tournamentService.removeTopic(aggregateId, deleteTopicEvent.getPublisherAggregateId(), deleteTopicEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processAnswerQuestionEvent(Integer aggregateId, QuizAnswerQuestionAnswerEvent quizAnswerQuestionAnswerEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        tournamentService.updateParticipantAnswer(aggregateId, quizAnswerQuestionAnswerEvent.getStudentAggregateId(), quizAnswerQuestionAnswerEvent.getPublisherAggregateId(), quizAnswerQuestionAnswerEvent.getQuestionAggregateId(), quizAnswerQuestionAnswerEvent.isCorrect(), quizAnswerQuestionAnswerEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processUnenrollStudentEvent(Integer aggregateId, UnerollStudentFromCourseExecutionEvent unerollStudentFromCourseExecutionEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        tournamentService.removeUser(aggregateId, unerollStudentFromCourseExecutionEvent.getPublisherAggregateId(), unerollStudentFromCourseExecutionEvent.getStudentAggregateId() ,unerollStudentFromCourseExecutionEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processInvalidateQuizEvent(Integer aggregateId, InvalidateQuizEvent invalidateQuizEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        tournamentService.invalidateQuiz(aggregateId, invalidateQuizEvent.getPublisherAggregateId(), invalidateQuizEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processUpdateExecutionStudentNameEvent(Integer subscriberAggregateId, UpdateStudentNameEvent updateStudentNameEvent) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        tournamentService.updateUserName(subscriberAggregateId, updateStudentNameEvent.getPublisherAggregateId(), updateStudentNameEvent.getPublisherAggregateVersion(), updateStudentNameEvent.getStudentAggregateId(), updateStudentNameEvent.getUpdatedName(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
}
