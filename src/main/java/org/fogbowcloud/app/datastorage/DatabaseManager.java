package org.fogbowcloud.app.datastorage;

public interface DatabaseManager <T> {

    void save(T t);
    T retrieveById(String id);
    void update(T t);

}
