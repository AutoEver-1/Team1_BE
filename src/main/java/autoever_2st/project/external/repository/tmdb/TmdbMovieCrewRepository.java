package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMovieCrew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface TmdbMovieCrewRepository extends JpaRepository<TmdbMovieCrew, Long> {
    @Query("SELECT crew.tmdbMember.id FROM TmdbMovieCrew crew WHERE crew.tmdbMovieDetail.id = :tmdbId AND crew.job = 'Director'")
    List<Long> findDirectorIdsByTmdbId(@Param("tmdbId") Long tmdbId);
}
