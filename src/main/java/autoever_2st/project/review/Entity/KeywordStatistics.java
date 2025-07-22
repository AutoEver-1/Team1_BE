package autoever_2st.project.review.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "keyword_statistics")
@Getter
@Setter
@NoArgsConstructor
public class KeywordStatistics {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String keyword;
    
    private Integer count;
    
    private Set<Long> reviewIds;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    public KeywordStatistics(String keyword) {
        this.keyword = keyword;
        this.count = 0;
        this.reviewIds = new HashSet<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void incrementCount(Long reviewId) {
        if (this.reviewIds.add(reviewId)) {
            this.count++;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    public void decrementCount(Long reviewId) {
        if (this.reviewIds.remove(reviewId)) {
            this.count--;
            this.updatedAt = LocalDateTime.now();
        }
    }
} 