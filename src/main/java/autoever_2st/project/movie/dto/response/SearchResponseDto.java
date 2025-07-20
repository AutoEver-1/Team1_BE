package autoever_2st.project.movie.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SearchResponseDto {

    @JsonProperty("movieList")
    private MovieListResponseDto movieList;
}
