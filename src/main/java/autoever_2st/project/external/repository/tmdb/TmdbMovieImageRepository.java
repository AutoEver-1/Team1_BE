package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.ImageType;
import autoever_2st.project.external.entity.tmdb.TmdbMovieImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import static autoever_2st.project.external.entity.tmdb.ImageType.POSTER;

import java.util.Optional;

public interface TmdbMovieImageRepository extends JpaRepository<TmdbMovieImages, Long> {
    Optional<TmdbMovieImages> findFirstByTmdbMovieDetailId(Long tmdbMovieDetailId);

    @Query("SELECT mi FROM TmdbMovieImages mi WHERE mi.tmdbMovieDetail.id = :tmdbId AND mi.imageType = POSTER")
    Optional<TmdbMovieImages> findPosterByTmdbId(@Param("tmdbId") Long tmdbId);

    Optional<TmdbMovieImages> findByTmdbMovieDetail_IdAndImageType(Long tmdbId, ImageType imageType);

    Optional<TmdbMovieImages> findFirstByTmdbMovieDetail_IdAndImageTypeOrderByIdAsc(Long tmdbId, ImageType imageType);

}
