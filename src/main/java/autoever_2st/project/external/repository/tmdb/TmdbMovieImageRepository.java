package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.ImageType;
import autoever_2st.project.external.entity.tmdb.TmdbMovieImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface TmdbMovieImageRepository extends JpaRepository<TmdbMovieImages, Long> {
    Optional<TmdbMovieImages> findFirstByTmdbMovieDetailId(Long tmdbMovieDetailId);

    @Query("SELECT mi FROM TmdbMovieImages mi WHERE mi.tmdbMovieDetail.id = :tmdbId AND mi.imageType = POSTER")
    Optional<TmdbMovieImages> findPosterByTmdbId(@Param("tmdbId") Long tmdbId);

    Optional<TmdbMovieImages> findByTmdbMovieDetail_IdAndImageType(Long tmdbId, ImageType imageType);

    Optional<TmdbMovieImages> findFirstByTmdbMovieDetail_IdAndImageTypeOrderByIdAsc(Long tmdbId, ImageType imageType);


    @Query("""
    SELECT mi FROM TmdbMovieImages mi
    WHERE mi.tmdbMovieDetail.id = :tmdbId
      AND mi.imageType = :imageType
    ORDER BY
      CASE 
        WHEN mi.iso6391 = 'ko' THEN 1
        WHEN mi.iso6391 = 'en' THEN 2
        WHEN mi.iso6391 IS NULL THEN 3
        ELSE 4
      END,
      mi.id ASC
    """)
    Optional<TmdbMovieImages> findPreferredPosterByTmdbId(
            @Param("tmdbId") Long tmdbId,
            @Param("imageType") ImageType imageType
    );
    @Query("SELECT m FROM TmdbMovieImages m JOIN FETCH m.tmdbMovieDetail d WHERE d.id IN :tmdbMovieDetailIds AND m.iso6391 = 'en' AND m.imageType = 'POSTER' ")
    List<TmdbMovieImages> findAllByIso6391AndTmdbMovieDetailIds(@Param("tmdbMovieDetailIds") List<Long> tmdbMovieDetailIds);
}
