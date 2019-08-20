package org.fogbowcloud.app.jes.arrebol;

/**
 * Interface that abstracts state synchronization operations between a local object and another
 * external object, that is, from an external service.
 *
 * @param <T> is the type of the local object that will be sync.
 */
public interface Synchronizer<T> {

    /**
     * Updates the internal state of the object passed for params. Can't
     *
     * @param object The object that will be updated.
     * @return A new object with the internal states updated.
     */
    T sync(T object);
}
