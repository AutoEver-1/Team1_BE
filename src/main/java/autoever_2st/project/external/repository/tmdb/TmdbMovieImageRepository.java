package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.ImageType;
import autoever_2st.project.external.entity.tmdb.TmdbMovieImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TmdbMovieImageRepository extends JpaRepository<TmdbMovieImages, Long> {
    Optional<TmdbMovieImages> findFirstByTmdbMovieDetail_IdAndImageTypeOrderByIdAsc(Long tmdbId, ImageType imageType);
    
    Optional<TmdbMovieImages> findFirstByTmdbMovieDetail_IdAndImageTypeAndIso6391AndRatioBetweenOrderByIdAsc(
            Long tmdbMovieDetailId, ImageType imageType, String iso6391, Double minRatio, Double maxRatio);
    
    List<TmdbMovieImages> findAllByTmdbMovieDetail_IdAndIso6391IsNull(Long tmdbMovieDetailId);

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

    @Query("SELECT m FROM TmdbMovieImages m JOIN FETCH m.tmdbMovieDetail d WHERE d.id IN :tmdbMovieDetailIds AND m.iso6391 = 'en' AND m.imageType = 'POSTER' AND m.imageUrl IS NOT NULL")
    List<TmdbMovieImages> findAllByIso6391AndTmdbMovieDetailIds(@Param("tmdbMovieDetailIds") List<Long> tmdbMovieDetailIds);

    @Query(nativeQuery = true, value = """
    SELECT DISTINCT movie_detail_id, base_url, image_url
    FROM (
        SELECT mi.tmdb_movie_detail_id as movie_detail_id, 
               mi.base_url, 
               mi.image_url,
               ROW_NUMBER() OVER (PARTITION BY mi.tmdb_movie_detail_id ORDER BY mi.id) as rn
        FROM tmdb_movie_images mi
        WHERE mi.tmdb_movie_detail_id IN (:movieDetailIds)
          AND mi.image_type = 'POSTER'
          AND mi.iso_639_1 = 'en'
          AND mi.ratio BETWEEN 0.0 AND 1.0
    ) ranked
    WHERE rn = 1
    """)
    List<Object[]> findPostersByMovieDetailIds(@Param("movieDetailIds") List<Long> movieDetailIds);
}
