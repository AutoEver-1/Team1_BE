package autoever_2st.project.review.Repository;

import autoever_2st.project.movie.entity.Movie;
import autoever_2st.project.review.Entity.Review;
import autoever_2st.project.user.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
//    // ✅ 영화와 회원으로 리뷰 찾기
//    Optional<Review> findByMemberAndMovie(Member member, Movie movie);
//    List<Review> findAllByMovieId(Long movieId);
// 특정 영화에 대한 모든 리뷰 가져오기 (member, reviewDetail, likes fetch join 포함)

    @Query("""
        SELECT DISTINCT r FROM Review r
        JOIN FETCH r.member m
        JOIN FETCH r.reviewDetail d
        LEFT JOIN FETCH r.likes l
        WHERE r.movie.id = :movieId
    """)
    List<Review> findAllByMovieId(@Param("movieId") Long movieId);
//    List<Review> findByMemberId(Long memberId);
    @Query("SELECT r FROM Review r JOIN FETCH r.movie WHERE r.member.id = :memberId")
    List<Review> findByMemberIdWithMovie(@Param("memberId") Long memberId);


    // 특정 회원이 특정 영화에 남긴 리뷰 (업데이트용)
    Optional<Review> findByMemberAndMovie(Member member, Movie movie);

    @Query("""
    SELECT r FROM Review r
    JOIN FETCH r.movie m
    JOIN FETCH m.tmdbMovieDetail d
    LEFT JOIN FETCH d.tmdbMovieImages imgs
    JOIN FETCH r.reviewDetail rd
    WHERE r.member.id = :memberId
""")
    List<Review> findWithMovieAndDetailsByMemberId(@Param("memberId") Long memberId);


    @Query("SELECT r.id FROM Review r WHERE r.member.id = :memberId")
    List<Long> findReviewIdsByMemberId(@Param("memberId") Long memberId);


    @Query("""
    SELECT DISTINCT r.movie.id 
    FROM Review r
    JOIN ReviewDetail rd ON rd.review.id = r.id
    WHERE r.member.id = :memberId AND rd.rating > 2.5
""")
    List<Long> findFavoriteMovieIdsByMemberId(@Param("memberId") Long memberId);

    @Query("""
    SELECT DISTINCT r.movie.id 
    FROM Review r
    JOIN ReviewDetail rd ON rd.review.id = r.id
    WHERE r.member.id = :memberId AND rd.rating < 2.5
""")
    List<Long> findDislikeMovieIdsByMemberId(@Param("memberId") Long memberId);


    boolean existsByMemberAndMovie(Member member, Movie movie);

    List<Review> findByMemberIdIn(List<Long> memberIds);
}
