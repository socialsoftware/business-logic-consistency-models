package pt.ulisboa.tecnico.socialsoftware.blcm.execution.event;

import org.springframework.scheduling.annotation.Scheduled;

public class CourseExecutionEventDetection {

    /*
        USER_EXISTS
     */
    @Scheduled(fixedDelay = 10000)
    public void detectRemoveUserEvents() {

    }

    /*
        COURSE_EXISTS
     */
}
