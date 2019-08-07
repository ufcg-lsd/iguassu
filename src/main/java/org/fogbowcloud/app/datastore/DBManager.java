package org.fogbowcloud.app.datastore;

public interface DBManager<T> {

    void save(T t);

    T findOne(String id);

    void update(T t);

    void delete(String id);

}
