package org.jboss.ws.tools.security.legacy;

/**
 * A helper class that caches and provides status about the SecurityManager API, which is deprecated starting from
 * JDK17 (JEP 411), and removed on JDK 23+ (JEP 486).
 * It also keeps the java.security.* deprecated APIs separate.
 */
public class SecurityManagerUtils {
    // Cache reflection based SecurityManager API state to avoid repeated computation
    private static final boolean SECURITY_MANAGER_AVAILABLE;

    static {
        boolean available = false;
        // Does getSecurityManager exist?
        // Note: according to https://docs.oracle.com/en/java/javase/24/security/security-manager-is-permanently-disabled.html
        // the Security Manager is permanently disabled starting from JDK 24.
        available = Runtime.version().feature() < 24;
        SECURITY_MANAGER_AVAILABLE = available;
    }

    /**
     * Provides access to the static boolean status flag that tells whether a SecurityManager is installed
     */
    public static boolean isSecurityManagerAvailable() {
        return SECURITY_MANAGER_AVAILABLE;
    }

    /**
     * Helper method to execute privileged action for getting context classloader, using FQN for
     * deprecated/removed APIs, and avoid class loading issues on JDK 23+
     */
    @SuppressWarnings("removal")
    public static ClassLoader doPrivilegedGetContextClassLoader()
    {
        return java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<ClassLoader>() {
                    public ClassLoader run()
                    {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
    }

    /**
     * Helper method to execute privileged action for getting context classloader, using FQN for
     * deprecated/removed APIs, and avoid class loading issues on JDK 23+
     */
    @SuppressWarnings("removal")
    public static ClassLoader doPrivilegedGetContextClassLoader(final Class<?> clazz)
    {
        return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<ClassLoader>() {
            public ClassLoader run()
            {
                return clazz.getClassLoader();
            }
        });
    }

    /**
     * Helper method to execute privileged action for setting context classloader, using FQN for
     * deprecated/removed APIs, and avoid class loading issues on JDK 23+
     */
    @SuppressWarnings("removal")
    public static void doPrivilegedSetContextClassLoader(final ClassLoader classLoader)
    {
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Object>()
        {
            public Object run()
            {
                Thread.currentThread().setContextClassLoader(classLoader);
                return null;
            }
        });
    }

    /**
     * Helper method to execute privileged action for loading a class, using FQN for
     * deprecated/removed APIs, and avoid class loading issues on JDK 23+
     */
    @SuppressWarnings("removal")
    public static Class<?> doPrivilegedLoadClass(final ClassLoader cl, final String name)
            throws java.security.PrivilegedActionException
    {
        return java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction<Class<?>>() {
            public Class<?> run() throws ClassNotFoundException {
                    return cl.loadClass(name);
                }
            });
    }
}
