package org.fogbowcloud.app.core.datastore.repositories;

import org.fogbowcloud.app.core.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByAlias(String alias);
}
