package pt.ulisboa.tecnico.socialsoftware.blcm.quiz;

public class QuizDto {
    public Integer id;
    public QuizDto(Quiz quiz) {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
