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

import org.jboss.ws.tools.security.legacy.SecurityManagerUtils;

/**
 * Security actions for this package.
 * Keep both pre-JDK23 (with SecurityManager) and JDK23+ (without SecurityManager) approach
 *
 * @author alessio.soldano@jboss.com
 * @author fburzigo@ibm.com
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
      if (!SecurityManagerUtils.isSecurityManagerAvailable())
      {
         return Thread.currentThread().getContextClassLoader();
      }
      else
      {
         return SecurityManagerUtils.doPrivilegedGetContextClassLoader();
      }
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
      else {
         SecurityManagerUtils.doPrivilegedSetContextClassLoader(classLoader);
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
      if (!SecurityManagerUtils.isSecurityManagerAvailable())
      {
         return clazz.getClassLoader();
      }
      else
      {
         return SecurityManagerUtils.doPrivilegedGetContextClassLoader(clazz);
      }
   }
}
