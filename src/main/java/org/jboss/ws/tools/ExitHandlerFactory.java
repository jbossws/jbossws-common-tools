package org.jboss.ws.tools;

import java.util.function.Supplier;

/**
 * Provides an {@link ExitHandler} concrete implementation, used to override the behavior in cases where
 * {@code System.exit} is called.
 */
public interface ExitHandlerFactory extends Supplier<ExitHandler> {}
