package org.jboss.test.ws.tools;

import org.jboss.ws.tools.ExitHandler;
import org.jboss.ws.tools.ExitHandlerFactory;

/**
 * Provides a concreate implementation of {@link ExitHandler} to be used by test code, where custom behavior should
 * take place rather than pure {@code System.exit()} calls.
 * Thread-safe singleton - only one instance is ever created.
 */
class TestExitHandlerFactory implements ExitHandlerFactory
{
    private static final TestExitHandlerFactory INSTANCE = new TestExitHandlerFactory();

    private TestExitHandlerFactory() {}

    public static TestExitHandlerFactory getInstance()
    {
        return INSTANCE;
    }

    @Override
    public ExitHandler get()
    {
        return TestExitHandler.getInstance();
    }
}
