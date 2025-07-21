package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMovieVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TmdbMovieVideoRepository extends JpaRepository<TmdbMovieVideo, Long> {
    Optional<TmdbMovieVideo> findFirstByTmdbMovieDetail_IdAndIso6391AndVideoTypeOrderByIdAsc(
            Long tmdbMovieDetailId, String iso6391, String videoType);
} 