package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TmdbMemberRepository extends JpaRepository<TmdbMember, Long> {
    TmdbMember findByTmdbId(Long tmdbId);

    @Query("SELECT m FROM TmdbMember m WHERE m.tmdbId IN :tmdbIds")
    List<TmdbMember> findAllByTmdbIdIn(List<Long> tmdbIds);
}
