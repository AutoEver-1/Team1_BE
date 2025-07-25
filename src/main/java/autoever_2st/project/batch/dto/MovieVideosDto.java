package autoever_2st.project.batch.dto;

import autoever_2st.project.external.dto.tmdb.response.movie.VideoDto;
import lombok.Getter;

import java.util.List;

/**
 * 영화 ID, TMDB ID와 비디오 목록을 함께 담는 DTO
 */
@Getter
public class MovieVideosDto {
    private final Long movieId;
    private final Long tmdbId;
    private final List<VideoDto> videos;

    public MovieVideosDto(Long movieId, Long tmdbId, List<VideoDto> videos) {
        this.movieId = movieId;
        this.tmdbId = tmdbId;
        this.videos = videos;
    }

    public MovieVideosDto(Long tmdbId, List<VideoDto> videos) {
        this.movieId = null;
        this.tmdbId = tmdbId;
        this.videos = videos;
    }
}