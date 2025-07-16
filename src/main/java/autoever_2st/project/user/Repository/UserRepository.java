package autoever_2st.project.user.Repository;


import autoever_2st.project.user.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Member, Integer> {

    Boolean existsByEmail(String email);

    Member findByEmail(String email);

}
