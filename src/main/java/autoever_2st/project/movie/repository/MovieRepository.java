package autoever_2st.project.movie.repository;

import autoever_2st.project.movie.entity.Movie;
import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findById(Long id);

    public List<Movie> findAllByKoficMovieDetailNotNull();
    
    Optional<Movie> findByTmdbMovieDetail(TmdbMovieDetail tmdbMovieDetail);

    @Query("SELECT m FROM Movie m WHERE m.tmdbMovieDetail.id IN :tmdbMovieDetailIds")
    List<Movie> findAllByTmdbMovieDetailIds(@Param("tmdbMovieDetailIds") List<Long> tmdbMovieDetailIds);

//    @Query("SELECT r.movie FROM Review r WHERE r.member.id = :memberId ORDER BY r.likeCount DESC")
//    List<Movie> findFavoriteMoviesByMemberIdOrderByLikeCountDesc(@Param("memberId") Long memberId);
}
