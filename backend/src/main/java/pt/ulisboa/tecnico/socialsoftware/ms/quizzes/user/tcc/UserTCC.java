package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.tcc;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.CausalConsistency;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.dto.UserDto;

import java.util.HashSet;
import java.util.Set;

@Entity
public class UserTCC extends User implements CausalConsistency {
    public UserTCC() {
        super();
    }

    public UserTCC(UserTCC other) {
        super(other);
    }

    public UserTCC(Integer aggregateId, UserDto userDto) {
        super(aggregateId, userDto);
    }
    @Override
    public Set<String> getFieldsChangedByFunctionalities() {
        return Set.of("name", "username", "active");
    }

    @Override
    public Set<String[]> getIntentions() {
        return new HashSet<>();
    }

    @Override
    public Aggregate mergeFields(Set<String> toCommitVersionChangedFields, Aggregate committedVersion, Set<String> committedVersionChangedFields) {
        return this;
    }
}
