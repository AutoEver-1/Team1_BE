package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMovieImages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TmdbMovieImageRepository extends JpaRepository<TmdbMovieImages, Long> {
    Optional<TmdbMovieImages> findFirstByTmdbMovieDetailId(Long tmdbMovieDetailId);
}
