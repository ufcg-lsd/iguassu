package org.fogbowcloud.app.datastore;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.auth.User;
import org.fogbowcloud.app.datastore.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class UserDBManager implements DBManager<User> {
    private static final Logger logger = Logger.getLogger(UserDBManager.class);

    private static UserDBManager instance;

    @Autowired
    private UserRepository userRepository;

    private UserDBManager() {}

    public synchronized static UserDBManager getInstance() {
        if (instance == null) {
            instance = new UserDBManager();
        }
        return instance;
    }


    @Override
    public void save(User user) {
        this.userRepository.save(user);
        logger.info("User " + user.getName() + " was saved.");
    }

    @Override
    public User findOne(String id) {
        return this.userRepository.findOne(id);
    }

    public User findUserByName(String name) {
        return this.userRepository.findUserByName(name);
    }

    @Override
    public void update(User user) {
        this.userRepository.delete(user);
        this.userRepository.save(user);
        logger.info("User " + user.getName() + " was updated.");
    }

    @Override
    public void delete(String id) {
        this.userRepository.delete(id);
    }
}
