package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMovieVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TmdbVideoRepository extends JpaRepository<TmdbMovieVideo, Long> {
    @Query("SELECT v FROM TmdbMovieVideo v WHERE v.tmdbMovieDetail.id IN :ids AND v.videoType = :videoType")
    List<TmdbMovieVideo> findAllByTmdbMovieDetailIdsAndVideoType(@Param("ids") List<Long> ids, @Param("videoType") String videoType);

    List<TmdbMovieVideo> findByTmdbMovieDetail_TmdbId(Long tmdbId);

    Optional<TmdbMovieVideo> findFirstByTmdbMovieDetail_IdAndIso6391AndVideoTypeOrderByIdAsc(
            Long tmdbMovieDetailId, String iso6391, String videoType);
}
