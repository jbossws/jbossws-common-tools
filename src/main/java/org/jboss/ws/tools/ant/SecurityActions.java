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
package org.jboss.ws.tools.ant;

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
final class SecurityActions
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
    * @param cl the classloader
    * @return previous context classloader
    * @throws Throwable for any error
    */
   static ClassLoader setContextClassLoader(final ClassLoader cl)
   {
      if (System.getSecurityManager() == null)
      {
         ClassLoader result = Thread.currentThread().getContextClassLoader();
         if (cl != null)
            Thread.currentThread().setContextClassLoader(cl);
         return result;
      }
      else
      {
         try
         {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<ClassLoader>() {
               public ClassLoader run() throws Exception
               {
                  try
                  {
                     ClassLoader result = Thread.currentThread().getContextClassLoader();
                     if (cl != null)
                        Thread.currentThread().setContextClassLoader(cl);
                     return result;
                  }
                  catch (Exception e)
                  {
                     throw e;
                  }
                  catch (Error e)
                  {
                     throw e;
                  }
                  catch (Throwable e)
                  {
                     throw new RuntimeException("Error setting context classloader", e);
                  }
               }
            });
         }
         catch (PrivilegedActionException e)
         {
            throw new RuntimeException("Error running privileged action", e.getCause());
         }
      }
   }
   
   /**
    * Get classloader from class.
    *
    * @param clazz the class
    * @return class's classloader
    */
   static ClassLoader getClassLoader(final Class<?> clazz)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return clazz.getClassLoader();
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run()
            {
               return clazz.getClassLoader();
            }
         });
      }
   }

}
