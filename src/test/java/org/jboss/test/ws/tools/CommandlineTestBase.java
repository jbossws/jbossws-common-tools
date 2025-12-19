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
import org.jboss.ws.tools.ExitHandler;

import java.util.Arrays;

/**
 * Base class for command line tests that need to intercept System.exit() calls.
 * Uses System Lambda library to overcome SecurityManager API deprecation/removal.
 *
 * @author Heiko.Braun@jboss.com
 */
public abstract class CommandlineTestBase extends TestCase
{
   /**
    * Exception thrown when exit is intercepted during testing.
    */
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

   /**
    * Execute a command with the given arguments and intercept exit calls.
    *
    * @param arguments Command line arguments (space-separated)
    * @param expectedException True if an exception or non-zero exit code is expected
    * @throws Exception If the test fails or an unexpected error occurs
    */
   protected void executeCmd(String arguments, boolean expectedException) throws Exception
   {
      String[] args = arguments != null ? arguments.split("\\s") : new String[0];

      try
      {
         // Call delegate: MUST use ExitHandler, specifically testExitHandler here in test
         runDelegate(args);

         // If we get here without an exception, the tool completed without calling exit
         if (expectedException)
         {
            fail("Did expect exception on args: " + Arrays.toString(args));
         }
      }
      catch (InterceptedExit e)
      {
         // Exit was called - check if the status matches expectations
         boolean positiveStatus = (e.getExitCode() == 0);

         if ((expectedException && positiveStatus) || (!expectedException && !positiveStatus))
         {
            String s = expectedException ? "Did expect an exception, but " : "Did not expect an exception, but ";
            String s2 = positiveStatus ? "status was positive" : "status was negative";
            throw new Exception(s + s2);
         }
      }
   }

   // the actual tools execution
   abstract void runDelegate(String[] args) throws Exception;
   
}
