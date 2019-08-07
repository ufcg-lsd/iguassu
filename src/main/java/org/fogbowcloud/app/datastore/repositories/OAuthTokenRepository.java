package org.fogbowcloud.app.datastore.repositories;

import org.fogbowcloud.app.core.models.auth.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
    OAuthToken findByUserId(String userId);

}
