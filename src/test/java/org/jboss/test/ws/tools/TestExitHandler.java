package org.jboss.test.ws.tools;

import org.jboss.ws.tools.ExitHandler;

/**
 * Test {@link ExitHandler} implementation, that throws {@link CommandlineTestBase.InterceptedExit} instead of exiting.
 * Used during testing to prevent JVM termination, and to perform assertions on the plugin behavior.
 * Implemented as a thread-safe singleton.
 */
class TestExitHandler implements ExitHandler
{
    private static final TestExitHandler INSTANCE = new TestExitHandler();

    private TestExitHandler() {}

    public static TestExitHandler getInstance() {
        return INSTANCE;
    }

    public void exit(int status)
    {
        String msg = (status == 0) ? "Delegate did exit without errors" : "Delegate did exit with an error";
        throw new CommandlineTestBase.InterceptedExit(msg, status);
    }
}

