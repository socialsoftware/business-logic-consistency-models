package pt.ulisboa.tecnico.socialsoftware.blcm.question.event;

import org.springframework.scheduling.annotation.Scheduled;

public class QuestionEventDetection {
    /*
        TOPICS_EXISTS
     */
    @Scheduled(fixedDelay = 10000)
    public void detectUpdateTopicEvents() {

    }

    /*
        TOPICS_EXISTS
     */
    @Scheduled(fixedDelay = 10000)
    public void detectDeleteTopicEvents() {

    }

    /*
        COURSE_EXISTS
     */
}
