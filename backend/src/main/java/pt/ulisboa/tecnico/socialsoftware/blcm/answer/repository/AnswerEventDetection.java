package pt.ulisboa.tecnico.socialsoftware.blcm.answer.repository;

import org.springframework.scheduling.annotation.Scheduled;

public class AnswerEventDetection {
    /*
        USER_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void detectDeleteUserEvents() {

    }

    /*
        QUESTION_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void detectUpdateQuestionEvents() {

    }

    /*
        QUESTION_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void detectRemoveQuestionEvents() {

    }

    /*
        QUIZ_EXISTS
     */
}
