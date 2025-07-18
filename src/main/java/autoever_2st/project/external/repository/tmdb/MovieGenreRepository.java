package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
    Optional<MovieGenre> findByGenreId(Long genreId);
}
