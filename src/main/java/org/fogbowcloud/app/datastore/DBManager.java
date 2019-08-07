package org.fogbowcloud.app.datastore;

public interface DBManager<T> {

    void save(T t);
    T retrieveById(String id);
    void update(T t);

}
