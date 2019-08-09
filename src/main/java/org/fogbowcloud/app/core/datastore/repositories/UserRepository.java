package org.fogbowcloud.app.core.datastore.repositories;

import org.fogbowcloud.app.core.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByAlias(String alias);
}
