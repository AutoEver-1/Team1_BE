package autoever_2st.project.external.dto.tmdb.common.movie;

import autoever_2st.project.external.dto.tmdb.response.movie.AlternativeMovieTitleDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class AlternativeMovieTitleWrapperDto {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("titles")
    private List<AlternativeMovieTitleDto> titles;

}
