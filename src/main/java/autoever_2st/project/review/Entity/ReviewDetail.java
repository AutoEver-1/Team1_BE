package autoever_2st.project.review.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Setter @Getter
public class ReviewDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "review_id", unique = true) // 유니크 제약
    private Review review;

    private Double rating;
    private String content;
    private LocalDateTime createdAt;
    @ColumnDefault("false")
    private Boolean isBanned;

    public void setBanned(Boolean banned) {
        this.isBanned = banned;
    }
}
