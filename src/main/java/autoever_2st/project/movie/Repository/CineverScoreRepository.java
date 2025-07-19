package autoever_2st.project.movie.Repository;

import autoever_2st.project.movie.entity.CineverScore;
import autoever_2st.project.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CineverScoreRepository extends JpaRepository<CineverScore, Long> {
    Optional<CineverScore> findByMovie(Movie movie);
}
