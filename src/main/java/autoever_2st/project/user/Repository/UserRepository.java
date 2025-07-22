package autoever_2st.project.user.Repository;


import autoever_2st.project.user.Entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("SELECT m FROM Member m WHERE m.name IS NOT NULL AND LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Member> findAllByNameContaining(@Param("name") String name, Pageable pageable);

    @Query("SELECT m FROM Member m WHERE m.nickname LIKE %:nickname%")
    Page<Member> findAllByNicknameContaining(@Param("nickname") String nickname, Pageable pageable);

    Optional<Member> findByNickname(String nickname);

    // 닉네임에 특정 문자열 포함하는 유저 리스트 조회 (List 버전)
    List<Member> findByNicknameContaining(String nickname);

    @Query("""
        SELECT m,
               COUNT(DISTINCT mf.follower.id) AS followerCount, 
               COUNT(DISTINCT r.id) AS reviewCount
        FROM Member m
        LEFT JOIN MemberFollower mf ON mf.member.id = m.id
        LEFT JOIN Review r ON r.member = m
        GROUP BY m
        ORDER BY followerCount DESC, m.nickname ASC
    """)
    List<Object[]> findAllOrderByFollowerCountDescAndNicknameAsc();
}
