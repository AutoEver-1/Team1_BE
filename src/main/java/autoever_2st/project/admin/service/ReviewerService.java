package autoever_2st.project.admin.service;


import autoever_2st.project.admin.dto.AdminReviewerDto;
import autoever_2st.project.admin.dto.request.ReviewerMultiBlockRequestDto;
import autoever_2st.project.admin.dto.request.ReviewerMultiRoleUpdateRequestDto;
import autoever_2st.project.admin.dto.request.ReviewerRoleUpdateRequestDto;
import autoever_2st.project.exception.exception_class.business.DataNotFoundException;
import autoever_2st.project.review.Repository.ReviewRepository;
import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Entity.Role;
import autoever_2st.project.user.Entity.RoleType;
import autoever_2st.project.user.Repository.RoleRepository;
import autoever_2st.project.user.Repository.UserRepository;
import autoever_2st.project.user.Repository.follow.FollowerRepository;
import autoever_2st.project.user.Repository.follow.MemberFollowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewerService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final MemberFollowerRepository memberFollowerRepository;
    private final RoleRepository roleRepository;

    public AdminReviewerDto findReviewerByNickname(String nickname) {
        Member member = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new DataNotFoundException("해당 닉네임의 리뷰어를 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()));

        int reviewCount = reviewRepository.countByMemberId(member.getId());

        long  followerCount = memberFollowerRepository.countByMemberId(member.getId());



        return new AdminReviewerDto(
                member.getId(),
                member.getRole().getName().name(),
                member.getNickname(),
                reviewCount,
                member.getProfileImgUrl(),
                followerCount,
                member.getIs_banned()
        );
    }

    public List<AdminReviewerDto> findReviewersByNicknameLike(String nickname) {
        List<Member> members = userRepository.findByNicknameContaining(nickname);

        return members.stream().map(member -> {
            int reviewCount = reviewRepository.countByMemberId(member.getId());
            long  followerCount = memberFollowerRepository.countByMemberId(member.getId());

            return new AdminReviewerDto(
                    member.getId(),
                    member.getRole().getName().name(),
                    member.getNickname(),
                    reviewCount,
                    member.getProfileImgUrl(),
                    followerCount,
                    member.getIs_banned()
            );
        }).collect(Collectors.toList());
    }

    public List<AdminReviewerDto> getAllReviewersOrderByFollowerAndNickname() {
        List<Object[]> result = userRepository.findAllOrderByFollowerCountDescAndNicknameAsc();

        return result.stream()
                .map(arr -> {
                    Member member = (Member) arr[0];
                    Long followerCount = (Long) arr[1];
                    Long reviewCountLong = (Long) arr[2];

                    Integer reviewCount = reviewCountLong != null ? reviewCountLong.intValue() : 0;

                    return new AdminReviewerDto(
                            member.getId(),
                            member.getRole().getName().name(), // role enum 이름
                            member.getNickname(),
                            reviewCount,
                            member.getProfileImgUrl(),
                            followerCount != null ? followerCount : 0L,
                            member.getIs_banned()
                    );
                })
                .collect(Collectors.toList());
    }

    //단일 역할 변경
    @Transactional
    public void updateReviewerRole(Long memberId, String newRoleStr) {
        Member member = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));

        RoleType newRoleType;
        try {
            newRoleType = RoleType.valueOf(newRoleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 역할 이름입니다: " + newRoleStr);
        }

        Role roleEntity = roleRepository.findByName(newRoleType)
                .orElseThrow(() -> new IllegalArgumentException("해당 역할이 존재하지 않습니다: " + newRoleType));

        member.setRole(roleEntity);

        userRepository.save(member);
    }

    //다중 역할 변경
    @Transactional
    public void updateMultiReviewerRole(ReviewerMultiRoleUpdateRequestDto requestDto) {
        for (ReviewerMultiRoleUpdateRequestDto.ReviewerRoleItem item : requestDto.getReviewerList()) {
            Long memberId = item.getMemberId();
            String newRoleStr = item.getRole();

            Member member = userRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다. memberId=" + memberId));

            RoleType newRoleType;
            try {
                newRoleType = RoleType.valueOf(newRoleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 역할 이름입니다: " + newRoleStr);
            }

            Role roleEntity = roleRepository.findByName(newRoleType)
                    .orElseThrow(() -> new IllegalArgumentException("해당 역할이 존재하지 않습니다: " + newRoleType));

            member.setRole(roleEntity);

            userRepository.save(member);
        }
    }

    //단일 차단
    @Transactional
    public void blockReviewer(Long memberId, Boolean isBanned) {
        Member member = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        member.setIs_banned(isBanned);

        userRepository.save(member); // 저장
    }

    // 단일 차단 해제
    @Transactional
    public void unblockReviewer(Long memberId, Boolean isBanned) {
        Member member = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        member.setIs_banned(isBanned);

        userRepository.save(member); // 저장
    }

    //다중 차단
    @Transactional
    public void blockMultiReviewer(ReviewerMultiBlockRequestDto requestDto) {
        for (ReviewerMultiBlockRequestDto.ReviewerBlockItem item : requestDto.getReviewerList()) {
            Long memberId = item.getMemberId();
            Boolean isBanned = item.getIsBanned();

            Member member = userRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다. memberId=" + memberId));

            member.setIs_banned(isBanned);
            userRepository.save(member);
        }
    }

    //다중 차단 풀기
    @Transactional
    public void unblockMultiReviewer(ReviewerMultiBlockRequestDto requestDto) {
        for (ReviewerMultiBlockRequestDto.ReviewerBlockItem item : requestDto.getReviewerList()) {
            Long memberId = item.getMemberId();
            Boolean isBanned = item.getIsBanned();

            Member member = userRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다. memberId=" + memberId));

            member.setIs_banned(isBanned);
            userRepository.save(member);
        }
    }




}
