/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.ws.tools.cmd;

import org.jboss.ws.tools.security.legacy.SecurityManagerUtils;

/**
 * Security actions for this package.
 * Keep both pre-JDK23 (with SecurityManager) and JDK23+ (without SecurityManager) approach
 *
 * @author alessio.soldano@jboss.com
 * @author fburzigo@ibm.com
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
      if (!SecurityManagerUtils.isSecurityManagerAvailable())
      {
         return Thread.currentThread().getContextClassLoader();
      }
      else
      {
         return doPrivilegedGetContextClassLoader();
      }
   }

   /**
    * Separate helper method to execute privileged action for getting context classloader, using FQN for
    * deprecated/removed APIs, and avoid class loading issues on JDK 23+
    */
   @SuppressWarnings("removal")
   private static ClassLoader doPrivilegedGetContextClassLoader()
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
    * Set context classloader.
    *
    * @param classLoader the classloader
    */
   static void setContextClassLoader(final ClassLoader classLoader)
   {
      if (!SecurityManagerUtils.isSecurityManagerAvailable())
      {
         Thread.currentThread().setContextClassLoader(classLoader);
      }
      else
      {
         SecurityManagerUtils.doPrivilegedSetContextClassLoader(classLoader);
      }
   }

   /**
    * Load a class using the provided classloader
    *
    * @param cl the classloader
    * @param name the class name
    * @return the loaded class
    * @throws java.security.PrivilegedActionException
    * @throws ClassNotFoundException
    */
   static Class<?> loadClass(final ClassLoader cl, final String name)
           throws java.security.PrivilegedActionException, ClassNotFoundException
   {
      if (!SecurityManagerUtils.isSecurityManagerAvailable())
      {
         return cl.loadClass(name);
      }
      else
      {
         return SecurityManagerUtils.doPrivilegedLoadClass(cl, name);
      }
   }

   /**
    * Get a system property
    *
    * @param name the property name
    * @param defaultValue the default value
    * @return the property value
    */
   static String getSystemProperty(final String name, final String defaultValue)
   {
      if (!SecurityManagerUtils.isSecurityManagerAvailable())
      {
         return System.getProperty(name, defaultValue);
      }
      else
      {
         return doPrivilegedGetSystemProperty(name, defaultValue);
      }
   }

   /**
    * Separate helper method to execute privileged action for getting system property, using FQN for
    * deprecated/removed APIs, and avoid class loading issues on JDK 23+
    */
   @SuppressWarnings("removal")
   private static String doPrivilegedGetSystemProperty(final String name, final String defaultValue)
   {
      java.security.PrivilegedAction<String> action =
              new java.security.PrivilegedAction<String>() {
                 public String run()
                 {
                    return System.getProperty(name, defaultValue);
                 }
              };
      return java.security.AccessController.doPrivileged(action);
   }
}
