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

import org.apache.tools.ant.types.CommandlineJava.SysProperties;
import org.apache.tools.ant.types.Environment.Variable;

/**
 * Helper class for ANT tasks.
 * 
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class AntTaskHelper
{
   /**
    * Constructor.
    */
   private AntTaskHelper()
   {
      // forbidden constructor
   }

   /**
    * Converts array of JVM arguments to ANT SysProperties object.
    * 
    * @param arguments to be converted.
    * @return ANT SysProperties object.
    */
   static SysProperties toSystemProperties(final String[] arguments)
   {
      final SysProperties retVal = new SysProperties();

      if (arguments != null && arguments.length != 0)
      {
         for (final String argument : arguments)
         {
            if (argument.startsWith("-D"))
            {
               Variable var = AntTaskHelper.toVariable(argument);
               retVal.addVariable(var);
            }
         }
      }

      return retVal;
   }

   /**
    * Converts JVM property of format -Dkey=value to ANT Variable object.
    *
    * @param argument to be converted
    * @return ANT Variable object
    */
   private static Variable toVariable(final String argument)
   {
      final Variable retVal = new Variable();
      final int equalSignIndex = argument.indexOf('=');

      if (equalSignIndex == -1)
      {
         final String key = argument.substring(2);
         retVal.setKey(key);
      }
      else
      {
         final String key = argument.substring(2, equalSignIndex);
         retVal.setKey(key);
         final String value = argument.substring(equalSignIndex + 1);
         retVal.setValue(value);
      }

      return retVal;
   }
}
