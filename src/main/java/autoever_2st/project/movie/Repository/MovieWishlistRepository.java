package autoever_2st.project.movie.Repository;

import autoever_2st.project.movie.entity.MovieWishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieWishlistRepository extends JpaRepository<MovieWishlist, Long> {
    List<MovieWishlist> findByMemberId(Long memberId);
}
