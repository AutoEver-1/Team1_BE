package autoever_2st.project.movie.dto.response;

import autoever_2st.project.movie.dto.MovieDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
public class MoviePageResponseDto extends PageResponseDto {

    @JsonProperty("movieList")
    Page<MovieDto> movieList;

    public MoviePageResponseDto(Page<MovieDto> movieList) {
        this.movieList = movieList;
    }
}
