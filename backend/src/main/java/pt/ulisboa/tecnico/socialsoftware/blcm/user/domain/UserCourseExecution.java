package pt.ulisboa.tecnico.socialsoftware.blcm.user.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class UserCourseExecution {
    @Column(name = "course_execution_aggregate_id")
    private Integer aggregateId;

    private String courseName;

    private String acronym;

    private String academicTerm;

    private Integer version;

    public UserCourseExecution() {}

    public UserCourseExecution (CourseExecutionDto courseExecutionDto) {
        setAggregateId((courseExecutionDto.getAggregateId()));
        setCourseName(courseExecutionDto.getName());
        setAcronym(courseExecutionDto.getAcronym());
        setAcademicTerm(courseExecutionDto.getAcademicTerm());
        setVersion(courseExecutionDto.getVersion());
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    // TODO discuss this with teacher
    public CourseExecutionDto buildDto() {
        CourseExecutionDto courseExecutionDto = new CourseExecutionDto();
        courseExecutionDto.setAggregateId(this.aggregateId);
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
        return this.getAggregateId() == other.getAggregateId() &&
                this.getCourseName().equals(other.getCourseName()) &&
                this.getAcronym().equals(other.getAcronym()) &&
                this.getAcademicTerm().equals(other.getAcademicTerm());
    }
}
