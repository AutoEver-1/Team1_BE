package autoever_2st.project.user.Repository.follow;

import autoever_2st.project.user.Entity.Follow.Following;
import autoever_2st.project.user.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowingRepository extends JpaRepository<Following, Long> {
    Optional<Following> findByMember_Id(Long memberId);
    Optional<Following> findByMember(Member member);
}
