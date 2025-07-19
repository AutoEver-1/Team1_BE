package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.MovieGenreMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieGenreMatchRepository extends JpaRepository<MovieGenreMatch, Long> {
    List<MovieGenreMatch> findByTmdbMovieDetailId(Long tmdbMovieDetailId);
}
