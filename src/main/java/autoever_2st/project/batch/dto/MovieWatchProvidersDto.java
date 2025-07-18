package autoever_2st.project.batch.dto;

import autoever_2st.project.external.dto.tmdb.response.movie.WatchProvidersDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 영화 ID와 해당 영화의 OTT 제공자 목록을 담는 DTO
 * 배치 처리 과정에서 Reader, Processor, Writer 간 데이터 전달에 사용.
 */
@Getter
@NoArgsConstructor
public class MovieWatchProvidersDto {
    private Long tmdbMovieDetailId; // TmdbMovieDetail 엔티티의 ID
    private Long tmdbId; // TMDB API의 영화 ID (tmdb_id)
    private Set<WatchProvidersDto.ProviderInner> providers;

    public MovieWatchProvidersDto(Long tmdbMovieDetailId, Long tmdbId, Set<WatchProvidersDto.ProviderInner> providers) {
        this.tmdbMovieDetailId = tmdbMovieDetailId;
        this.tmdbId = tmdbId;
        this.providers = providers;
    }
}