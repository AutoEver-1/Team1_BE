package autoever_2st.project.movie.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cinever_score")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CineverScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY) // 1:1 관계로 가정
    @JoinColumn(name = "movie_id", referencedColumnName = "id", nullable = false)
    private Movie movie;

    private double score;

    @Column(name = "review_count")
    private int reviewCount;
}
