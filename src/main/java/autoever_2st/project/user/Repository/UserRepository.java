package autoever_2st.project.user.Repository;


import autoever_2st.project.user.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Member, Integer> {

    Boolean existsByEmail(String email);
}
