package org.fogbowcloud.app.datastore;

public interface DBManager<T> {

    void save(T t);

    T findOne(long id);

    void update(T t);

    void delete(long id);

}
