package autoever_2st.project.review.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "review_keywords")
@Getter
@Setter
@NoArgsConstructor
public class ReviewKeyword {
    
    @Id
    private String id;
    
    private Long reviewId;
    
    private List<String> keywords;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    public ReviewKeyword(Long reviewId, List<String> keywords) {
        this.reviewId = reviewId;
        this.keywords = keywords;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
} 