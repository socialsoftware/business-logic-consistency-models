package pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork;

public class AggregateIdTypePair {
    private Integer objectId;

    private Integer prevObjectId;

    private String type;

    private boolean isCommited;

    public AggregateIdTypePair(Integer objectId, String type) {
        this.objectId = objectId;
        this.type = type;
        this.isCommited = false;
    }

    public AggregateIdTypePair(Integer objectId, Integer prevObjectId, String type) {
        this.objectId = objectId;
        this.prevObjectId = prevObjectId;
        this.type = type;
        this.isCommited = false;
    }

    public Integer getObjectId() {
        return objectId;
    }

    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    public Integer getPrevObjectId() {
        return prevObjectId;
    }

    public void setPrevObjectId(Integer prevObjectId) {
        this.prevObjectId = prevObjectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isCommited() {
        return isCommited;
    }

    public void setCommited(boolean commited) {
        isCommited = commited;
    }
}
