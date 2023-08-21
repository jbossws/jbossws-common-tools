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
package org.jboss.test.ws.tools;

import junit.framework.TestCase;

import java.security.Permission;

/**
 * @author Heiko.Braun@jboss.com
 */
public abstract class CommandlineTestBase extends TestCase
{
   SecurityManager systemDefault = System.getSecurityManager();
   SecurityManager interceptor = new InterceptedSecurity();

   protected void swapSecurityManager()
   {
      if(System.getSecurityManager() instanceof InterceptedSecurity)
         System.setSecurityManager(systemDefault);
      else
         System.setSecurityManager(interceptor);
   }

   class InterceptedSecurity extends  SecurityManager
   {
      private final SecurityManager parent = systemDefault;

      public void checkPermission(Permission perm)
      {
         if (parent != null)
         {
            parent.checkPermission(perm);
         }
      }

      public void checkExit(int status)
      {
         String msg = (status == 0) ? "Delegate did exit without errors" : "Delegate did exit with an error";
         throw new InterceptedExit(msg, status);
      }
   }

   static protected class InterceptedExit extends SecurityException
   {
      private static final long serialVersionUID = 1L;
      private int exitCode;

      public InterceptedExit(String s, int code)
      {
         super(s);
         this.exitCode = code;
      }


      public int getExitCode()
      {
         return exitCode;
      }
   }

   protected void executeCmd(String arguments,  boolean expectedException) throws Exception
   {
      swapSecurityManager();

      String[] args = arguments!=null ? arguments.split("\\s"): new String[0];
      try
      {
         runDelegate(args);
         if(expectedException)
            fail("Did expect exception on args: " +args);
      }
      catch (CommandlineTestBase.InterceptedExit e)
      {
         boolean positivStatus = (e.getExitCode() == 0);
         if( (expectedException && positivStatus)
           || (!expectedException && !positivStatus) )
         {
            String s = expectedException ? "Did expect an exception, but " : "Did not expect an exception, but ";
            String s2 = positivStatus ? "status was positiv" : "status was negativ";
            throw new Exception(s+s2);
         }

      }
      finally
      {
         swapSecurityManager();
      }
   }

   // the actual tools execution
   abstract void runDelegate(String[] args) throws Exception;
   
}
