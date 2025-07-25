package autoever_2st.project.user.Service;

import autoever_2st.project.user.Entity.Follow.Follower;
import autoever_2st.project.user.Entity.Follow.Following;
import autoever_2st.project.user.Entity.Follow.MemberFollower;
import autoever_2st.project.user.Entity.Follow.MemberFollowing;
import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Repository.UserRepository;
import autoever_2st.project.user.Repository.follow.FollowerRepository;
import autoever_2st.project.user.Repository.follow.FollowingRepository;
import autoever_2st.project.user.Repository.follow.MemberFollowerRepository;
import autoever_2st.project.user.Repository.follow.MemberFollowingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final UserRepository userRepository;
    private final FollowerRepository followerRepository;
    private final FollowingRepository followingRepository;
    private final MemberFollowerRepository memberFollowerRepository;
    private final MemberFollowingRepository memberFollowingRepository;

    // 팔로우
    public void follow(Long myMemberId, Long targetMemberId) {
        if (myMemberId.equals(targetMemberId)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }

        Member me = userRepository.findById(myMemberId)
                .orElseThrow(() -> new EntityNotFoundException("내 정보 없음"));

        Member target = userRepository.findById(targetMemberId)
                .orElseThrow(() -> new EntityNotFoundException("팔로우 대상 없음"));

        // 중복 팔로우 방지
        if (memberFollowerRepository.existsByMember_IdAndFollower_Id(target.getId(), me.getId())) {
            throw new IllegalStateException("이미 팔로우한 사용자입니다.");
        }

        // follower 테이블에 '나'가 존재하지 않으면 생성
        Follower follower = followerRepository.findByMember(me)
                .orElseGet(() -> followerRepository.save(new Follower(me)));

        // following 테이블에 '상대방'이 없으면 생성
        Following following = followingRepository.findByMember(target)
                .orElseGet(() -> followingRepository.save(new Following(target)));

        // member_follower 저장 (내가 팔로우한 사람: target)
        MemberFollower memberFollower = new MemberFollower();
        memberFollower.setMember(target);    // 팔로우 당한 사람
        memberFollower.setFollower(me); // 내가 팔로우한 정보
        memberFollowerRepository.save(memberFollower);

        // member_following 저장 (내가 팔로우한 사람: target)
        MemberFollowing memberFollowing = new MemberFollowing();
        memberFollowing.setMember(me);       // 나
        memberFollowing.setFollowing(target); // 내가 팔로우한 대상
        memberFollowingRepository.save(memberFollowing);
    }

    // 언팔로우
    public void unfollow(Long myMemberId, Long targetMemberId) {
        Member me = userRepository.findById(myMemberId)
                .orElseThrow(() -> new EntityNotFoundException("내 정보 없음"));

        Member target = userRepository.findById(targetMemberId)
                .orElseThrow(() -> new EntityNotFoundException("언팔로우 대상 없음"));

        // member_follower 엔티티 찾기 (target이 팔로우 당함, me가 팔로워)
        MemberFollower memberFollower = memberFollowerRepository
                .findByMember_IdAndFollower_Id(target.getId(), me.getId())
                .orElseThrow(() -> new EntityNotFoundException("팔로우 관계가 존재하지 않습니다."));

        // member_following 엔티티 찾기 (me가 팔로우, target이 대상)
        MemberFollowing memberFollowing = memberFollowingRepository
                .findByMember_IdAndFollowing_Id(me.getId(), target.getId())
                .orElseThrow(() -> new EntityNotFoundException("팔로잉 관계가 존재하지 않습니다."));

        // 삭제
        memberFollowerRepository.delete(memberFollower);
        memberFollowingRepository.delete(memberFollowing);

        // follower 테이블 정리: 내가 더 이상 아무도 안 팔로우하면 제거
        boolean stillFollowing = memberFollowingRepository.existsByMember_Id(me.getId());
        if (!stillFollowing) {
            followerRepository.findByMember(me).ifPresent(followerRepository::delete);
        }

        // following 테이블 정리: 상대방을 더 이상 아무도 안 팔로우하면 제거
        boolean stillBeingFollowed = memberFollowerRepository.existsByMember_Id(target.getId());
        if (!stillBeingFollowed) {
            followingRepository.findByMember(target).ifPresent(followingRepository::delete);
        }
    }
}
