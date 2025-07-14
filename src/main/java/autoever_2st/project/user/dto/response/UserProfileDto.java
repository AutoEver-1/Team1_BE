package autoever_2st.project.user.dto.response;

import autoever_2st.project.user.dto.UserFollowerDto;
import autoever_2st.project.user.dto.UserWishlistItemDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
public class UserProfileDto {
    private Long memberId;
    private Boolean isFollowing;
    private Boolean isFollower;
    private String nickname;
    private Integer followerCount;
    private List<UserFollowerDto> followerList;
    private Integer followingCount;
    private List<UserFollowerDto> followingList;
    private String gender;
    private Date birth;
    private String profilePath;
    private String roleName;
    private List<String> preferenceGenre;
    private List<UserWishlistItemDto> wishList;

    public UserProfileDto(Long memberId, Boolean isFollowing, Boolean isFollower, String nickname,
                         Integer followerCount, List<UserFollowerDto> followerList,
                         Integer followingCount, List<UserFollowerDto> followingList,
                         String gender, Date birth, String profilePath, String roleName,
                         List<String> preferenceGenre, List<UserWishlistItemDto> wishList) {
        this.memberId = memberId;
        this.isFollowing = isFollowing;
        this.isFollower = isFollower;
        this.nickname = nickname;
        this.followerCount = followerCount;
        this.followerList = followerList;
        this.followingCount = followingCount;
        this.followingList = followingList;
        this.gender = gender;
        this.birth = birth;
        this.profilePath = profilePath;
        this.roleName = roleName;
        this.preferenceGenre = preferenceGenre;
        this.wishList = wishList;
    }
}