package autoever_2st.project.user.Repository.follow;


import autoever_2st.project.user.Entity.Follow.Follower;
import autoever_2st.project.user.Entity.Follow.MemberFollower;
import autoever_2st.project.user.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberFollowerRepository extends JpaRepository<MemberFollower, Long> {

    boolean existsByMember_IdAndFollower_Id(Long memberId, Long followerId);
    boolean existsByMember_Id(Long memberId);
    Optional<MemberFollower> findByMember_IdAndFollower_Id(Long memberId, Long followerId);

    boolean existsByMemberIdAndFollowerId(Long memberId, Long followerId);

    // 특정 유저를 팔로우하는 사람 수 (팔로워 수)
    long countByMemberId(Long memberId);
    List<MemberFollower> findAllByMemberId(Long memberId);

    // 내가 팔로우하는 사람 수
    int countByFollower_Id(Long followerId);
}
