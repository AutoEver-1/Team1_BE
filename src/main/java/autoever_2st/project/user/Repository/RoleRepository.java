package autoever_2st.project.user.Repository;


import autoever_2st.project.user.Entity.Role;
import autoever_2st.project.user.Entity.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository  extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(RoleType name);
}
