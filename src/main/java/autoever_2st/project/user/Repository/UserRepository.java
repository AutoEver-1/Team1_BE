package autoever_2st.project.user.Repository;


import autoever_2st.project.user.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Member, Long> {

    Boolean existsByEmail(String email);
    //Member findByEmail(String email);
    Optional<Member> findByEmail(String email);

}
