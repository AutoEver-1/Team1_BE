package autoever_2st.project.movie.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class RandomMovieResponseDto {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("quote")
    private String quote;

    @JsonProperty("author")
    private String author;

    @JsonProperty("name")
    private String name;
}
