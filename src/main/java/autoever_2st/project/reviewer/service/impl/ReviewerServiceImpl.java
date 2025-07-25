package autoever_2st.project.reviewer.service.impl;

import autoever_2st.project.movie.repository.MovieWishlistRepository;
import autoever_2st.project.review.Repository.ReviewDetailRepository;
import autoever_2st.project.review.Repository.ReviewRepository;
import autoever_2st.project.reviewer.dto.GenrePreferenceDto;
import autoever_2st.project.reviewer.dto.ReviewerDto;
import autoever_2st.project.reviewer.dto.WishlistItemDto;
import autoever_2st.project.reviewer.service.ReviewerService;
import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Entity.MemberGenrePreference;
import autoever_2st.project.user.Repository.MemberGenrePreferenceRepository;
import autoever_2st.project.user.Repository.UserRepository;
import autoever_2st.project.user.Repository.follow.MemberFollowerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewerServiceImpl implements ReviewerService {

    private final UserRepository userRepository;
    private final MemberFollowerRepository memberFollowerRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewDetailRepository reviewDetailRepository;
    private final MemberGenrePreferenceRepository memberGenrePreferenceRepository;
    private final MovieWishlistRepository movieWishlistRepository;

    @Override
    public List<ReviewerDto> getAllReviewers() {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewerDto> getAllReviewersSortedByFollowerCount(Pageable pageable) {
        // 팔로워 수와 리뷰 수로 정렬된 멤버 목록 조회
        List<Object[]> memberData = userRepository.findAllOrderByFollowerCountDescAndNicknameAsc();
        
        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), memberData.size());
        
        if (start >= memberData.size()) {
            return new PageImpl<>(List.of(), pageable, memberData.size());
        }
        
        List<Object[]> pagedData = memberData.subList(start, end);
        
        // Member ID 리스트 추출 (벌크 조회를 위해)
        List<Long> memberIds = pagedData.stream()
                .map(data -> ((Member) data[0]).getId())
                .collect(Collectors.toList());
        
        // 벌크로 장르 선호도 조회
        Map<Long, List<GenrePreferenceDto>> genrePreferencesMap = getGenrePreferencesMap(memberIds);
        
        // 벌크로 위시리스트 조회
        Map<Long, List<WishlistItemDto>> wishlistMap = getWishlistMap(memberIds);
        
        // 벌크로 리뷰 평균 점수 조회
        Map<Long, Double> reviewAverageMap = getReviewAverageMap(memberIds);
        
        // ReviewerDto 생성
        List<ReviewerDto> reviewerDtos = pagedData.stream()
                .map(data -> {
                    Member member = (Member) data[0];
                    Long followerCount = (Long) data[1];
                    Long reviewCount = (Long) data[2];
                    
                    return new ReviewerDto(
                            member.getId(),
                            member.getRole() != null ? member.getRole().getName().name() : "ROLE_USER",
                            member.getNickname(),
                            reviewCount.intValue(),
                            member.getProfileImgUrl(),
                            genrePreferencesMap.getOrDefault(member.getId(), List.of()),
                            followerCount.intValue(),
                            reviewAverageMap.getOrDefault(member.getId(), 0.0),
                            wishlistMap.getOrDefault(member.getId(), List.of()),
                            member.getIs_banned()
                    );
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(reviewerDtos, pageable, memberData.size());
    }
    
    /**
     * 멤버 ID 리스트로 장르 선호도 맵 조회 (N+1 최적화)
     */
    private Map<Long, List<GenrePreferenceDto>> getGenrePreferencesMap(List<Long> memberIds) {
        List<MemberGenrePreference> allPreferences = memberGenrePreferenceRepository.findAll().stream()
                .filter(pref -> memberIds.contains(pref.getMember().getId()))
                .collect(Collectors.toList());
        
        return allPreferences.stream()
                .collect(Collectors.groupingBy(
                    pref -> pref.getMember().getId(),
                    Collectors.mapping(
                        pref -> new GenrePreferenceDto(
                            pref.getMovieGenre().getName(),
                            pref.getValue()
                        ),
                        Collectors.toList()
                    )
                ));
    }
    
    /**
     * 멤버 ID 리스트로 위시리스트 맵 조회 (N+1 최적화 - QueryDSL 사용)
     */
    private Map<Long, List<WishlistItemDto>> getWishlistMap(List<Long> memberIds) {
        return movieWishlistRepository.findWishlistItemsByMemberIds(memberIds, 3);
    }
    
    /**
     * 멤버 ID 리스트로 리뷰 평균 점수 맵 조회 (N+1 최적화)
     */
    private Map<Long, Double> getReviewAverageMap(List<Long> memberIds) {
        return memberIds.stream()
                .collect(Collectors.toMap(
                    memberId -> memberId,
                    memberId -> {
                        List<Double> ratings = reviewRepository.findAllByMemberId(memberId).stream()
                                .map(review -> {
                                    if (review.getReviewDetail() != null) {
                                        return review.getReviewDetail().getRating();
                                    }
                                    return null;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        
                        if (ratings.isEmpty()) {
                            return 0.0;
                        }
                        
                        return ratings.stream()
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(0.0);
                    }
                ));
    }
}
