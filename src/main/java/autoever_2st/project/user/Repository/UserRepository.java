package autoever_2st.project.user.Repository;


import autoever_2st.project.user.Entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Member, Long> {
    @Query("SELECT m FROM Member m " +
            "LEFT JOIN FETCH m.role " +
            "LEFT JOIN FETCH m.followers " +  // followers만 fetch join
            "WHERE m.id = :id")
    Optional<Member> findWithRoleAndFollowersById(@Param("id") Long id);


    Boolean existsByEmail(String email);
    //Member findByEmail(String email);
    Optional<Member> findByEmail(String email);
    Optional<Member> findById(Long memberId);

    @Query("SELECT m FROM Member m WHERE m.name LIKE %:name%")
    Page<Member> findAllByNameContaining(@Param("name") String name, Pageable pageable);

}
