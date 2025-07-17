package autoever_2st.project.user.Repository;

import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Entity.MemberGenrePreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberGenrePreferenceRepository extends JpaRepository<MemberGenrePreference, Long> {
    List<MemberGenrePreference> findByMember(Member member);
}
