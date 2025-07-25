package autoever_2st.project.user.Repository;

import autoever_2st.project.user.Entity.OAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthRepository extends JpaRepository<OAuth, Long> {
    Optional<OAuth> findByOauthTypeAndProviderId(String oauthType, String providerId);
}
