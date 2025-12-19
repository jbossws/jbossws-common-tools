package org.jboss.ws.tools;

/**
 * Interface for handling program exits. Used to define a dynamic behavior when handling events where
 * {@code System.exit()} is called.
 * <p>
 *     Tools/commands should use this interface instead of calling System.exit() directly, in order to allow proper
 *     management.
 * </p>
 * Usage example:<br>
 * Usual code:
 *
 *   public static void main(String[] args) {
 *     if (error) {
 *       System.exit(1);
 *     }
 *     System.exit(0);
 *   }
 *
 * Adapted code:
 *   private static ExitHandler exitHandler = new SystemExitHandler();
 *
 *   public static void setExitHandler(ExitHandler handler) {
 *     exitHandler = handler;
 *   }
 *
 *   public static void main(String[] args) {
 *     if (error) {
 *       exitHandler.exit(1);
 *     }
 *     exitHandler.exit(0);
 *   }
 */
public interface ExitHandler
{
    /**
     * Handle program exit with the given status code.
     *
     * @param status Exit status code (0 for success, non-zero for error)
     */
    void exit(int status);
}
