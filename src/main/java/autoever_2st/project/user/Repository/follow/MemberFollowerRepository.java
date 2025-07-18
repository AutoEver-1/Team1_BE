package autoever_2st.project.user.Repository.follow;


import autoever_2st.project.user.Entity.Follow.Follower;
import autoever_2st.project.user.Entity.Follow.MemberFollower;
import autoever_2st.project.user.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberFollowerRepository extends JpaRepository<MemberFollower, Long> {

//    Optional<MemberFollower> findByMemberAndFollower(Member member, Follower follower);
//
//    boolean existsByMemberAndFollower(Member member, Follower follower);
//
//    void deleteByMemberAndFollower(Member member, Follower follower);

//    boolean existsByMember_IdAndFollower_Id(Long memberId, Long followerId);
//    Optional<MemberFollower> findByMember_IdAndFollower_Id(Long memberId, Long followerId);
//    void deleteByMember_IdAndFollower_Id(Long memberId, Long followerId);


    boolean existsByMember_IdAndFollower_Id(Long memberId, Long followerId);
    boolean existsByMember_Id(Long memberId);
    Optional<MemberFollower> findByMember_IdAndFollower_Id(Long memberId, Long followerId);

}
