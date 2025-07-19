package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
    Optional<MovieGenre> findByGenreId(Long genreId);

    @Query("SELECT mg.name FROM MovieGenre mg WHERE mg.genreId IN :genreIds")
    List<String> findNamesByGenreIds(@Param("genreIds") List<Long> genreIds);

}
