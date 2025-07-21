package autoever_2st.project.user.Repository.follow;

import autoever_2st.project.user.Entity.Follow.Follower;
import autoever_2st.project.user.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, Long> {

    Optional<Follower> findByMember_Id(Long memberId);
    Optional<Follower> findByMember(Member member);

}
