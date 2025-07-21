package autoever_2st.project.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewFromFollowingResponseDto {
    private Long movieId;
    private String title;
    private String posterPath;
    private LocalDate releaseDate;
    private Double averageScore;
    private Boolean isAdult;
    private List<String> movieGenre;

    private String followingRole;
    private String followingProfilePath;
    private String followingNickname;
    private Long followingMemId;

    private Double rating;
    private LocalDate reviewedDate;
    private String context;
    private Long likeCount;
    private Boolean likeByMe;
}
