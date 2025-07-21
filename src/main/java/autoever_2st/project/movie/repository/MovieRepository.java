package autoever_2st.project.movie.repository;

import autoever_2st.project.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findById(Long id);

    public List<Movie> findAllByKoficMovieDetailNotNull();

}
