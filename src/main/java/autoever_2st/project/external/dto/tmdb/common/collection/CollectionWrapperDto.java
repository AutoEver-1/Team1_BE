package autoever_2st.project.external.dto.tmdb.common.collection;

import autoever_2st.project.external.dto.tmdb.response.collection.CollectionResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class CollectionWrapperDto {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("overview")
    private String overview;
    @JsonProperty("poster_path")
    private String posterPath;
    @JsonProperty("backdrop_path")
    private String backdropPath;
    @JsonProperty("parts")
    private List<CollectionResponseDto> parts;
}
