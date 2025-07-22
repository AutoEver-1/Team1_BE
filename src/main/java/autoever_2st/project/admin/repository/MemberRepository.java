package autoever_2st.project.admin.repository;

import autoever_2st.project.user.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface MemberRepository extends JpaRepository<Member, Long> {
    //가입자 추세
    @Query(value = "SELECT COUNT(*) FROM member WHERE DATE(registed_at) = :date", nativeQuery = true)
    int countByCreatedDate(@Param("date") LocalDate date);

    @Query(value = "SELECT COUNT(*) FROM member WHERE YEAR(registed_at) = :year AND MONTH(registed_at) = :month", nativeQuery = true)
    int countByCreatedMonth(@Param("year") int year, @Param("month") int month);

    //탈퇴자 추세
    @Query(value = "SELECT COUNT(*) FROM member WHERE DATE(deleted_at) = :date", nativeQuery = true)
    int countByWithdrawalDate(@Param("date") LocalDate date);

    @Query(value = "SELECT COUNT(*) FROM member WHERE YEAR(deleted_at) = :year AND MONTH(deleted_at) = :month", nativeQuery = true)
    int countByWithdrawalMonth(@Param("year") int year, @Param("month") int month);
}
