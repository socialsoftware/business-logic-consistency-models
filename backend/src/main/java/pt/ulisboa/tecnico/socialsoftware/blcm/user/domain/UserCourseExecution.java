package pt.ulisboa.tecnico.socialsoftware.blcm.user.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

import javax.persistence.Embeddable;

@Embeddable
public class UserCourseExecution {
    private Integer courseExecutionAggregateId;

    private String courseName;

    private String acronym;

    private String academicTerm;

    public UserCourseExecution() {}

    public UserCourseExecution(Integer courseExecutionAggregateId, String courseName, String acronym, String academicTerm) {
        setCourseExecutionAggregateId(courseExecutionAggregateId);
        setCourseName(courseName);
        setAcronym(acronym);
        setAcademicTerm(academicTerm);
    }

    public UserCourseExecution (CourseExecutionDto courseExecutionDto) {
        setCourseExecutionAggregateId((courseExecutionDto.getAggregateId()));
        setCourseName(courseExecutionDto.getName());
        setAcronym(courseExecutionDto.getAcronym());
        setAcademicTerm(courseExecutionDto.getAcademicTerm());
    }

    public Integer getCourseExecutionAggregateId() {
        return courseExecutionAggregateId;
    }

    public void setCourseExecutionAggregateId(Integer aggregateId) {
        this.courseExecutionAggregateId = aggregateId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getAcademicTerm() {
        return academicTerm;
    }

    public void setAcademicTerm(String academicTerm) {
        this.academicTerm = academicTerm;
    }

    // TODO discuss this with teacher
    public CourseExecutionDto buildDto() {
        CourseExecutionDto courseExecutionDto = new CourseExecutionDto();
        courseExecutionDto.setAggregateId(this.courseExecutionAggregateId);
        courseExecutionDto.setName(courseExecutionDto.getName());
        courseExecutionDto.setAcronym(this.acronym);
        courseExecutionDto.setAcademicTerm(this.academicTerm);
        return courseExecutionDto;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof UserCourseExecution) {
            return false;
        }

        UserCourseExecution other = (UserCourseExecution) obj;
        return this.getCourseExecutionAggregateId() == other.getCourseExecutionAggregateId() &&
                this.getCourseName().equals(other.getCourseName()) &&
                this.getAcronym().equals(other.getAcronym()) &&
                this.getAcademicTerm().equals(other.getAcademicTerm());
    }
}
