package org.jboss.ws.tools;

/**
 * Default ExitHandler that actually calls System.exit().
 * Used in production code.
 * Implemented as a thread-safe singleton.
 */
public class SystemExitHandler implements ExitHandler
{
    private static final SystemExitHandler INSTANCE = new SystemExitHandler();

    private SystemExitHandler() {}

    public static SystemExitHandler getInstance() {
        return INSTANCE;
    }

    public void exit(int status)
    {
        System.exit(status);
    }
}
