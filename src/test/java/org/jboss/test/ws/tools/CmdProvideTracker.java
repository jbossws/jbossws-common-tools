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

import org.jboss.ws.api.tools.WSContractProvider;

import java.io.File;
import java.io.PrintStream;

/**
 * @author Heiko.Braun@jboss.com
 */
public class CmdProvideTracker extends WSContractProvider
{

   public static String LAST_EVENT = "";

   public void setGenerateWsdl(boolean generateWsdl)
   {
      LAST_EVENT += "setGenerateWsdl";
   }

   public void setExtension(boolean extension)
   {
      LAST_EVENT += "setExtension";
   }

   public void setGenerateSource(boolean generateSource)
   {
      LAST_EVENT += "setGenerateSource";
   }

   public void setOutputDirectory(File directory)
   {
      LAST_EVENT += "setOutputDirectory";
   }

   public void setResourceDirectory(File directory)
   {
      LAST_EVENT += "setResourceDirectory";
   }

   public void setSourceDirectory(File directory)
   {
      LAST_EVENT += "setSourceDirectory";
   }

   public void setClassLoader(ClassLoader loader)
   {
      LAST_EVENT += "setClassLoader";
   }

   public void provide(String endpointClass)
   {
      LAST_EVENT += "provide";   
   }

   public void provide(Class<?> endpointClass)
   {
      LAST_EVENT += "provide";
   }

   public void setMessageStream(PrintStream messageStream)
   {
      LAST_EVENT += "setMessageStream";
   }

   public void setPortSoapAddress(String address)
   {
      LAST_EVENT += "setPortSoapAddress";
   }
}
