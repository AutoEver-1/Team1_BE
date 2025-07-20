package autoever_2st.project.movie.Repository;

import autoever_2st.project.movie.entity.MovieWishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieWishlistRepository extends JpaRepository<MovieWishlist, Long> {
    List<MovieWishlist> findByMemberId(Long memberId);

    @Query("SELECT mw.movie.id FROM MovieWishlist mw WHERE mw.member.id = :memberId ORDER BY mw.id DESC")
    List<Long> findMovieIdsByMemberIdOrderByIdDesc(@Param("memberId") Long memberId);

    @Query("SELECT mw.movie.id FROM MovieWishlist mw WHERE mw.member.id = :memberId")
    List<Long> findMovieIdsByMemberId(@Param("memberId") Long memberId);
}
