package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.event;

import org.springframework.scheduling.annotation.Scheduled;

public class QuizEventDetection {
    /*
        COURSE_EXECUTION_EXISTS
     */
    @Scheduled(fixedDelay = 10000)
    public void detectRemoveCourseExecutionEvent() {

    }

    /*
        QUESTION_EXISTS
     */
    @Scheduled(fixedDelay = 10000)
    public void detectUpdateQuestionEvent() {

    }

    /*
        QUESTION_EXISTS
     */
    @Scheduled(fixedDelay = 10000)
    public void detectRemoveQuestionEvent() {

    }
}
