package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMovieCast;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TmdbMovieCastRepository extends JpaRepository<TmdbMovieCast, Long> {
    List<TmdbMovieCast> findAllByTmdbMovieDetailId(Long tmdbMovieDetailId);
}