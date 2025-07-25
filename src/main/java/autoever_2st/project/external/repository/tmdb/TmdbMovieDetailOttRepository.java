package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMovieDetailOtt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TmdbMovieDetailOttRepository extends JpaRepository<TmdbMovieDetailOtt, Long> {
    List<TmdbMovieDetailOtt> findAllByTmdbMovieDetailId(Long tmdbMovieDetailId);
} 