/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ws.tools.cmd;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * Security actions for this package
 * 
 * @author alessio.soldano@jboss.com
 * @since 19-Jun-2009
 *
 */
class SecurityActions
{
   /**
    * Get context classloader.
    * 
    * @return the current context classloader
    */
   static ClassLoader getContextClassLoader()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return Thread.currentThread().getContextClassLoader();
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         });
      }
   }
   
   /**
    * Set context classloader.
    *
    * @param classLoader the classloader
    */
   static void setContextClassLoader(final ClassLoader classLoader)
   {
      if (System.getSecurityManager() == null)
      {
         Thread.currentThread().setContextClassLoader(classLoader);
      }
      else
      {
         AccessController.doPrivileged(new PrivilegedAction<Object>()
         {
            public Object run()
            {
               Thread.currentThread().setContextClassLoader(classLoader);
               return null;
            }
         });
      }
   }

   static ClassLoader getModulesClassLoader()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return getModulesClassLoaderInternal();
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run()
            {
               return getModulesClassLoaderInternal();
            }
         });
      }
   }
   
   private static ClassLoader getModulesClassLoaderInternal()
   {
       // TODO: use SPI class loader facade, not reflection!
       try {
          Class<?> moduleClass = Class.forName("org.jboss.modules.Module");
          Class<?> moduleIdentifierClass = Class.forName("org.jboss.modules.ModuleIdentifier");
          Class<?> moduleLoaderClass = Class.forName("org.jboss.modules.ModuleLoader");
          Object moduleLoader = moduleClass.getMethod("getBootModuleLoader").invoke(null);
          Object moduleIdentifier = moduleIdentifierClass.getMethod("create", String.class).invoke(null, "org.jboss.as.webservices.server.integration");
          Object module = moduleLoaderClass.getMethod("loadModule", moduleIdentifierClass).invoke(moduleLoader, moduleIdentifier);
          return (ClassLoader)moduleClass.getMethod("getClassLoader").invoke(module);
       } catch (Exception e) {
          //ignore, JBoss Modules might not be available at all
          return null;
       }
   }

   /**
    * Load a class using the provided classloader
    * 
    * @param name
    * @return
    * @throws PrivilegedActionException
    */
   static Class<?> loadClass(final ClassLoader cl, final String name) throws PrivilegedActionException, ClassNotFoundException
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return cl.loadClass(name);
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
            public Class<?> run() throws PrivilegedActionException
            {
               try
               {
                  return cl.loadClass(name);
               }
               catch (Exception e)
               {
                  throw new PrivilegedActionException(e);
               }
            }
         });
      }
   }

   /**
    * Get a system property
    * 
    * @param name
    * @param defaultValue
    * @return
    */
   static String getSystemProperty(final String name, final String defaultValue)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return System.getProperty(name, defaultValue);
      }
      else
      {
         PrivilegedAction<String> action = new PrivilegedAction<String>() {
            public String run()
            {
               return System.getProperty(name, defaultValue);
            }
         };
         return AccessController.doPrivileged(action);
      }
   }
}
