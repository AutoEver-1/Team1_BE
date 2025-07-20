package autoever_2st.project.movie.repository;

import autoever_2st.project.movie.entity.MovieWishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MovieWishlistRepository extends JpaRepository<MovieWishlist, Long> {
    List<MovieWishlist> findByMemberId(Long memberId);

    @Query("SELECT mw.movie.id FROM MovieWishlist mw WHERE mw.member.id = :memberId ORDER BY mw.id DESC")
    List<Long> findMovieIdsByMemberIdOrderByIdDesc(@Param("memberId") Long memberId);

    @Query("SELECT mw.movie.id FROM MovieWishlist mw WHERE mw.member.id = :memberId")
    List<Long> findMovieIdsByMemberId(@Param("memberId") Long memberId);

    //이미 찜한 영화인지 중복 여부를 체크
    boolean existsByMemberIdAndMovieId(Long memberId, Long movieId);

    //특정 회원(memberId)이 특정 영화(movieId)를 찜한 기록을 찾기 위함
    Optional<MovieWishlist> findByMemberIdAndMovieId(Long memberId, Long movieId);

}
