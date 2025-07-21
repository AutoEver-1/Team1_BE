package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.MovieGenreMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieGenreMatchRepository extends JpaRepository<MovieGenreMatch, Long>, MovieGenreMatchRepositoryCustom {
    @Query("SELECT m FROM MovieGenreMatch m WHERE m.tmdbMovieDetail.id = :tmdbId")
    List<MovieGenreMatch> findByTmdbMovieDetailId(@Param("tmdbId") Long tmdbId);

    @Query("SELECT DISTINCT m.movieGenre.id FROM MovieGenreMatch m")
    List<Long> findGenreIdsByTmdbId(@Param("tmdbId") Long tmdbId);

    @Query("SELECT mg.movieGenre.name FROM MovieGenreMatch mg WHERE mg.tmdbMovieDetail.id = :tmdbMovieDetailId")
    List<String> findGenreNamesByTmdbMovieDetailId(@Param("tmdbMovieDetailId") Long tmdbMovieDetailId);

}
