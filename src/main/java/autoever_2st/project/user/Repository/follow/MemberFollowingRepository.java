package autoever_2st.project.user.Repository.follow;

import autoever_2st.project.user.Entity.Follow.MemberFollowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberFollowingRepository extends JpaRepository<MemberFollowing, Long> {
    //boolean existsByMember_IdAndFollowing_Id(Long memberId, Long followingId);
    boolean existsByMember_Id(Long memberId);
    Optional<MemberFollowing> findByMember_IdAndFollowing_Id(Long memberId, Long followingId);


    boolean existsByMemberIdAndFollowingId(Long memberId, Long followingId);
    long countByMemberId(Long memberId);
    List<MemberFollowing> findAllByMemberId(Long memberId);
}
