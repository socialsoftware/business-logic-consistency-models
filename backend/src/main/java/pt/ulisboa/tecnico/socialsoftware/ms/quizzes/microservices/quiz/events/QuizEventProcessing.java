package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.quiz.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.ms.causal.unityOfWork.CausalUnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.causal.unityOfWork.CausalUnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.events.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.question.events.publish.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.question.events.publish.UpdateQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.quiz.service.QuizService;

@Service
public class QuizEventProcessing {
    @Autowired
    private QuizService quizService;
    @Autowired
    private CausalUnitOfWorkService unitOfWorkService;

    public void processRemoveCourseExecutionEvent(Integer aggregateId, RemoveCourseExecutionEvent removeCourseExecutionEvent) {
        CausalUnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        quizService.removeCourseExecution(aggregateId, removeCourseExecutionEvent.getPublisherAggregateId(), removeCourseExecutionEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
    public void processUpdateQuestionEvent(Integer aggregateId, UpdateQuestionEvent updateQuestionEvent) {
        CausalUnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        quizService.updateQuestion(aggregateId, updateQuestionEvent.getPublisherAggregateId(), updateQuestionEvent.getTitle(), updateQuestionEvent.getContent(), updateQuestionEvent.getPublisherAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void processRemoveQuizQuestionEvent(Integer aggregateId, RemoveQuestionEvent removeQuestionEvent) {
        CausalUnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        quizService.removeQuizQuestion(aggregateId, removeQuestionEvent.getPublisherAggregateId(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
}
