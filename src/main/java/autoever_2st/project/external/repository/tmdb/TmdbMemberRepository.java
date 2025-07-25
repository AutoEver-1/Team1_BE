package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TmdbMemberRepository extends JpaRepository<TmdbMember, Long> {
    TmdbMember findByTmdbId(Long tmdbId);

    @Query("SELECT m FROM TmdbMember m WHERE m.tmdbId IN :tmdbIds")
    List<TmdbMember> findAllByTmdbIdIn(List<Long> tmdbIds);

    @Query("SELECT m FROM TmdbMember m JOIN m.tmdbMovieCrew c WHERE c.job = 'Director' AND m.name LIKE %:name%")
    Page<TmdbMember> findAllDirectorsByNameContaining(@Param("name") String name, Pageable pageable);

    @Query("SELECT m FROM TmdbMember m JOIN m.tmdbMovieCast c WHERE m.name LIKE %:name%")
    Page<TmdbMember> findAllActorsByNameContaining(@Param("name") String name, Pageable pageable);
}
