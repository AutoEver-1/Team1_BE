package autoever_2st.project.movie.service;

public interface MovieWishlistService {

    public void addMovieToWishlist(Long memberId, Long movieId);

    public void removeMovieFromWishlist(Long memberId, Long movieId);

}
