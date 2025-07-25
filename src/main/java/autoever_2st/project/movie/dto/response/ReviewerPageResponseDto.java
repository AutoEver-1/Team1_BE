package autoever_2st.project.movie.dto.response;

import autoever_2st.project.reviewer.dto.ReviewerDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
public class ReviewerPageResponseDto extends PageResponseDto {

    @JsonProperty("reviewerList")
    Page<ReviewerDto> reviewerList;

    public ReviewerPageResponseDto(Page<ReviewerDto> reviewerList) {
        this.reviewerList = reviewerList;
    }

}
