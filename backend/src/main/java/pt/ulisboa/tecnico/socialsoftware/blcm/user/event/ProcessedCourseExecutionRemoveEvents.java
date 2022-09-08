package pt.ulisboa.tecnico.socialsoftware.blcm.user.event;

import javax.persistence.*;

@Entity
@Table(name = "user_processed_remove_course_execution_events")
public class ProcessedCourseExecutionRemoveEvents {
    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private Integer lastProcessed;

    public ProcessedCourseExecutionRemoveEvents() {

    }

    public ProcessedCourseExecutionRemoveEvents(Integer lastProcessed) {
        this.lastProcessed = lastProcessed;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLastProcessed() {
        return lastProcessed;
    }

    public void setLastProcessed(Integer lastProcessed) {
        this.lastProcessed = lastProcessed;
    }
}
