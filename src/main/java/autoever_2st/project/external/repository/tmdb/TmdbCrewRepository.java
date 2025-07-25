package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMovieCrew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TmdbCrewRepository extends JpaRepository<TmdbMovieCrew, Long> {

    @Query("SELECT m FROM TmdbMovieCrew m JOIN FETCH m.tmdbMovieDetail d JOIN FETCH m.tmdbMember WHERE d.id IN :tmdbMovieDetailIds AND m.job = :job")
    List<TmdbMovieCrew> findAllByJobAndTmdbMovieDetailIds(String job, List<Long> tmdbMovieDetailIds);
}
