package autoever_2st.project.admin.service;


import autoever_2st.project.admin.dto.AdminReviewerDto;
import autoever_2st.project.exception.exception_class.business.DataNotFoundException;
import autoever_2st.project.review.Repository.ReviewRepository;
import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Repository.UserRepository;
import autoever_2st.project.user.Repository.follow.FollowerRepository;
import autoever_2st.project.user.Repository.follow.MemberFollowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewerService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final MemberFollowerRepository memberFollowerRepository;

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
}
