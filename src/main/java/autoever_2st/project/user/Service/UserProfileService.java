package autoever_2st.project.user.Service;

import autoever_2st.project.external.entity.tmdb.MovieGenre;
import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import autoever_2st.project.external.repository.tmdb.MovieGenreMatchRepository;
import autoever_2st.project.external.repository.tmdb.TmdbMovieImageRepository;
import autoever_2st.project.movie.repository.MovieWishlistRepository;
import autoever_2st.project.movie.entity.Movie;
import autoever_2st.project.user.Entity.Follow.MemberFollowing;
import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Repository.MemberGenrePreferenceRepository;
import autoever_2st.project.user.Repository.UserRepository;
import autoever_2st.project.user.Repository.follow.MemberFollowerRepository;
import autoever_2st.project.user.Repository.follow.MemberFollowingRepository;
import autoever_2st.project.user.dto.UserFollowerDto;
import autoever_2st.project.user.dto.UserWishlistItemDto;
import autoever_2st.project.user.dto.request.UserUpdateRequestDto;
import autoever_2st.project.user.dto.response.UserProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Transactional
public class UserProfileService {

    private final UserRepository userRepository;
    private final MemberFollowerRepository memberFollowerRepository;
    private final MemberFollowingRepository memberFollowingRepository;
    private final MemberGenrePreferenceRepository memberGenrePreferenceRepository;
    private final MovieWishlistRepository movieWishlistRepository;
    private final MovieGenreMatchRepository movieGenreMatchRepository;
    private final TmdbMovieImageRepository tmdbMovieImageRepository;

    public UserProfileDto getUserProfile(Long viewerId, Long targetMemberId) {
//        Member member = userRepository.findById(targetMemberId)
//                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
//        Member member = userRepository.findWithAllRelationsById(targetMemberId)
//                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        Member member = userRepository.findWithRoleAndFollowersById(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        boolean isMe = viewerId.equals(targetMemberId);
        boolean isFollowing = !isMe && memberFollowingRepository.existsByMemberIdAndFollowingId(viewerId, targetMemberId);
        boolean isFollower = !isMe && memberFollowerRepository.existsByMemberIdAndFollowerId(targetMemberId, viewerId);

        // ⭐ Followings 조회 (중복 제거!)
        List<MemberFollowing> followings = memberFollowingRepository.findAllByMemberId(targetMemberId);
        List<UserFollowerDto> followingList = followings.stream()
                .map(f -> {
                    Member followed = f.getFollowing();
                    return new UserFollowerDto(
                            followed.getNickname(),
                            followed.getProfileImgUrl(),
                            followed.getId()
                    );
                }).toList();
        int followingCount = followingList.size();

        // ✅ Followers (fetch join된 멤버에서 꺼냄)
        List<UserFollowerDto> followerList = member.getFollowers().stream()
                .map(f -> {
                    Member follower = f.getFollower();
                    return new UserFollowerDto(
                            follower.getNickname(),
                            follower.getProfileImgUrl(),
                            follower.getId()
                    );
                }).toList();
        int followerCount = followerList.size();

        // ✅ Role
        String roleName = member.getRole() != null ? member.getRole().getName().name() : "ROLE_USER";

        // ✅ Genre Preferences
        List<String> preferenceGenre = memberGenrePreferenceRepository.findByMemberIdOrderByValueDesc(targetMemberId)
                .stream()
                .map(pref -> {
                    MovieGenre genre = pref.getMovieGenre();
                    return genre != null ? genre.getName() : null;
                })
                .filter(Objects::nonNull)
                .toList();

        // ✅ WishList
        List<UserWishlistItemDto> wishList = movieWishlistRepository.findByMemberId(targetMemberId)
                .stream().map(wish -> {
                    Movie movie = wish.getMovie();
                    TmdbMovieDetail tmdbDetail = movie.getTmdbMovieDetail();

                    List<String> genres = movieGenreMatchRepository.findByTmdbMovieDetailId(tmdbDetail.getId())
                            .stream()
                            .map(match -> match.getMovieGenre().getName())
                            .filter(Objects::nonNull)
                            .toList();

                    String imageUrl = tmdbMovieImageRepository.findFirstByTmdbMovieDetailId(tmdbDetail.getId())
                            .map(img -> img.getBaseUrl() + img.getImageUrl())
                            .orElse(null);

                    return new UserWishlistItemDto(
                            movie.getId(),
                            imageUrl,
                            tmdbDetail.getTitle(),
                            genres
                    );
                }).toList();

        return new UserProfileDto(
                targetMemberId,
                isFollowing,
                isFollower,
                member.getNickname(),
                followerCount,
                followerList,
                followingCount,
                followingList,
                member.getGender(),
                member.getBirth_date(),
                member.getProfileImgUrl(),
                roleName,
                preferenceGenre,
                wishList
        );
    }


    @Transactional
    public void updateUserInfo(Long memberId, UserUpdateRequestDto request) {
        Member member = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (request.getNickname() != null) {
            member.setNickname(request.getNickname());
        }

        if (request.getProfilePath() != null) {
            member.setProfile_img_url(request.getProfilePath());
        }

        // 변경감지에 의해 JPA가 자동 업데이트
    }
}
