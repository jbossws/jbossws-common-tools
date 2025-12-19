package org.jboss.ws.tools;

/**
 * Provides a default implementation of {@link ExitHandler}, that is a call to {@code System.exit()}.
 * Thread-safe singleton - only one instance is ever created.
 */
public class SystemExitHandlerFactory implements ExitHandlerFactory
{
    private static final SystemExitHandlerFactory INSTANCE = new SystemExitHandlerFactory();

    private SystemExitHandlerFactory() {}

    public static SystemExitHandlerFactory getInstance()
    {
        return INSTANCE;
    }

    @Override
    public ExitHandler get()
    {
        return SystemExitHandler.getInstance();
    }
}

