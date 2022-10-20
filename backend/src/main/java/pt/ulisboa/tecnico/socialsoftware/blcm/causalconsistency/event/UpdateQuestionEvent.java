package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.UPDATE_QUESTION;

@Entity
@DiscriminatorValue(UPDATE_QUESTION)
public class UpdateQuestionEvent extends Event {
    private String title;

    private String content;

    // TODO put OPTION

    public UpdateQuestionEvent() {
        super();
    }
    public UpdateQuestionEvent(Integer aggregateId, String title, String content) {
        super(aggregateId);
        setTitle(title);
        setContent(content);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
