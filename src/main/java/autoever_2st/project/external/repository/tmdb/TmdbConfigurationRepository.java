package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TmdbConfigurationRepository extends JpaRepository<TmdbConfiguration, Long> {
    Optional<TmdbConfiguration> findTopByOrderByIdDesc();
}