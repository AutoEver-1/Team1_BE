package autoever_2st.project.movie.service;


import autoever_2st.project.movie.Repository.MovieRepository;
import autoever_2st.project.movie.Repository.MovieWishlistRepository;
import autoever_2st.project.movie.entity.Movie;
import autoever_2st.project.movie.entity.MovieWishlist;
import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovieWishlistService {
    private final MovieRepository movieRepository;
    private final MovieWishlistRepository movieWishlistRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addMovieToWishlist(Long memberId, Long movieId) {
        if (movieWishlistRepository.existsByMemberIdAndMovieId(memberId, movieId)) {
            throw new IllegalStateException("이미 찜한 영화입니다.");
        }

        Member member = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("영화가 존재하지 않습니다."));

        MovieWishlist wishlist = new MovieWishlist();
        wishlist.setMember(member);
        wishlist.setMovie(movie);

        movieWishlistRepository.save(wishlist);
    }

    @Transactional
    public void removeMovieFromWishlist(Long memberId, Long movieId) {
        MovieWishlist wishlist = movieWishlistRepository
                .findByMemberIdAndMovieId(memberId, movieId)
                .orElseThrow(() -> new IllegalArgumentException("찜하지 않은 영화입니다."));

        movieWishlistRepository.delete(wishlist);
    }

}
