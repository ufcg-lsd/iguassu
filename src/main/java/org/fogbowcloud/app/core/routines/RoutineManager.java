package org.fogbowcloud.app.core.routines;

/**
 * This interface defines operations that Iguassu routine managers should have, centralizing thread
 * handling.
 */
public interface RoutineManager {

    /**
     * Starts all the routines registered in the system.
     */
    void startAll();
}
