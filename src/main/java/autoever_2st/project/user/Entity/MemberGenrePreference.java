package autoever_2st.project.user.Entity;


import autoever_2st.project.external.entity.tmdb.MovieGenre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "member_genre_preference")
@Getter
@Setter
public class MemberGenrePreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 선호 장르
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_genre_id")
    private MovieGenre movieGenre;

    // 선호도 점수
    private Integer value;
}
