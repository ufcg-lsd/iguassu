package org.fogbowcloud.app.datastore.repositories;

import org.fogbowcloud.app.core.models.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByName(String name);
}
