package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.List;

@Repository
public interface TmdbMovieDetailRepository extends JpaRepository<TmdbMovieDetail, Long> {
    Optional<TmdbMovieDetail> findById(Long id);

    @Query("SELECT m FROM TmdbMovieDetail m WHERE m.tmdbId IN :tmdbIds")
    List<TmdbMovieDetail> findAllByTmdbIdIn(List<Long> tmdbIds);
}
