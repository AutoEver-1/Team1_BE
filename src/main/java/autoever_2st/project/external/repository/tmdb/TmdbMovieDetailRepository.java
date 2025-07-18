package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TmdbMovieDetailRepository extends JpaRepository<TmdbMovieDetail, Long> {
}
