package autoever_2st.project.batch.dto;

import autoever_2st.project.external.dto.tmdb.response.movie.MovieImageWithTypeDto;
import lombok.Getter;

import java.util.List;

/**
 * 영화 ID와 이미지 목록을 함께 담는 DTO
 */
@Getter
public class MovieImagesDto {
    private final Long movieId;
    private final List<MovieImageWithTypeDto> images;

    public MovieImagesDto(Long movieId, List<MovieImageWithTypeDto> images) {
        this.movieId = movieId;
        this.images = images;
    }
}