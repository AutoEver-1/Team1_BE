package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TmdbMemberRepository extends JpaRepository<TmdbMember, Long> {}
