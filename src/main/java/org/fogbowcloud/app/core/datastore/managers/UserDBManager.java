package org.fogbowcloud.app.core.datastore.managers;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.user.User;
import org.fogbowcloud.app.core.datastore.DBManager;
import org.fogbowcloud.app.core.datastore.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDBManager implements DBManager<User> {
    private static final Logger logger = Logger.getLogger(UserDBManager.class);

    @Autowired
    private static UserDBManager instance;

    @Autowired
    private UserRepository userRepository;

    private UserDBManager() {
    }

    public synchronized static UserDBManager getInstance() {
        if (instance == null) {
            instance = new UserDBManager();
        }
        return instance;
    }


    @Override
    public void save(User user) {
        this.userRepository.save(user);
        logger.info("User " + user.getAlias() + " was saved.");
    }

    @Override
    public User findOne(long id) {
        return this.userRepository.findById(id).isPresent() ? this.userRepository.findById(id).get() : null;
    }

    public User findUserByAlias(String alias) {
        return this.userRepository.findUserByAlias(alias);
    }

    public List<User> findAll() {
        return this.userRepository.findAll();
    }

    @Override
    public void update(User user) {
        this.userRepository.delete(user);
        this.userRepository.save(user);
        logger.info("User " + user.getAlias() + " was updated.");
    }

    @Override
    public void delete(long id) {
        this.userRepository.deleteById(id);
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
