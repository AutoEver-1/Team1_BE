package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.MovieGenreMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieGenreMatchRepository extends JpaRepository<MovieGenreMatch, Long> {
    List<MovieGenreMatch> findByTmdbMovieDetailId(Long tmdbMovieDetailId);

    @Query("SELECT mgm.movieGenre.id FROM MovieGenreMatch mgm WHERE mgm.tmdbMovieDetail.id = :tmdbId")
    List<Long> findGenreIdsByTmdbId(@Param("tmdbId") Long tmdbId);
}
