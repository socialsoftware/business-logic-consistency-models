package pt.ulisboa.tecnico.socialsoftware.blcm.exception;

public enum ErrorMessage {
    INVALID_AGGREGATE_TYPE("Aggregate type %s does not exist"),

    VERSION_MANAGER_DOES_NOT_EXIST("Version manager does not exist."),


    TOURNAMENT_NOT_FOUND("Tournament with aggregate Id %d does not exist."),
    TOURNAMENT_INVALID("Tournament version with id %d breaks invariants."),
    TOURNAMENT_MERGE_FAILURE("Two version of tournament with aggregate id %d cannot be merged."),
    TOURNAMENT_MISSING_USER("Tournament requires a user."),
    TOURNAMENT_MISSING_TOPICS("Tournament requires topics."),
    TOURNAMENT_MISSING_START_TIME("Tournament requires a start time."),
    TOURNAMENT_MISSING_END_TIME("Tournament requires an end time."),
    TOURNAMENT_MISSING_NUMBER_OF_QUESTIONS("Tournament requires a number of questions."),
    TOURNAMENT_DELETED("Tournament with aggregate id %d already deleted."),

    QUIZ_NOT_FOUND("Quiz with aggregate Id %d does not exist");

    public final String label;

    ErrorMessage(String label) {
        this.label = label;
    }
}
