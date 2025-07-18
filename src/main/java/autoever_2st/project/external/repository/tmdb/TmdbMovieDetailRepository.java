package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TmdbMovieDetailRepository extends JpaRepository<TmdbMovieDetail, Long> {
    Optional<TmdbMovieDetail> findById(Long id);
}
