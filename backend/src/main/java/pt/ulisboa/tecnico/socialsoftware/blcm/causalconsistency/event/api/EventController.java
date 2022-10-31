package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.event.AnswerEventDetection;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.CourseExecutionEventDetection;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.event.QuestionEventDetection;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.event.QuizEventDetection;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.event.TopicEventDetection;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event.TournamentEventDetection;

@RestController
public class EventController {
    private static final String SCHEDULED_TASKS = "scheduledTasks";

    @Autowired
    private ScheduledAnnotationBeanPostProcessor postProcessor;

    @Autowired
    private QuestionEventDetection questionEventDetection;

    @Autowired
    private TopicEventDetection topicEventDetection;

    @Autowired
    private CourseExecutionEventDetection executionEventDetection;

    @Autowired
    private AnswerEventDetection answerEventDetection;

    @Autowired
    private QuizEventDetection quizEventDetection;

    @Autowired
    private TournamentEventDetection tournamentEventDetection;

    @GetMapping(value = "/scheduler/stop")
    public String stopSchedule() {
        postProcessor.postProcessBeforeDestruction(questionEventDetection, SCHEDULED_TASKS);
        postProcessor.postProcessBeforeDestruction(topicEventDetection, SCHEDULED_TASKS);
        postProcessor.postProcessBeforeDestruction(executionEventDetection, SCHEDULED_TASKS);
        postProcessor.postProcessBeforeDestruction(answerEventDetection, SCHEDULED_TASKS);
        postProcessor.postProcessBeforeDestruction(quizEventDetection, SCHEDULED_TASKS);
        postProcessor.postProcessBeforeDestruction(tournamentEventDetection, SCHEDULED_TASKS);
        System.out.println("Stopped event detection");
        return "OK";
    }

    @GetMapping(value = "/scheduler/start")
    public String startSchedule() {
        postProcessor.postProcessAfterInitialization(questionEventDetection, SCHEDULED_TASKS);
        postProcessor.postProcessAfterInitialization(topicEventDetection, SCHEDULED_TASKS);
        postProcessor.postProcessAfterInitialization(executionEventDetection, SCHEDULED_TASKS);
        postProcessor.postProcessAfterInitialization(answerEventDetection, SCHEDULED_TASKS);
        postProcessor.postProcessAfterInitialization(quizEventDetection, SCHEDULED_TASKS);
        postProcessor.postProcessAfterInitialization(tournamentEventDetection, SCHEDULED_TASKS);
        System.out.println("Started event detection");
        return "OK";
    }
}
