package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.CompanyMovie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyMovieRepository extends JpaRepository<CompanyMovie, Long> {
    List<CompanyMovie> findAllByMovieId(Long movieId);
} 