package autoever_2st.project.external.dto.tmdb.response.collection;

import autoever_2st.project.external.dto.tmdb.response.movie.MovieResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CollectionResponseDto extends MovieResponseDto {
    @JsonProperty("media_type")
    private String mediaType;
}
