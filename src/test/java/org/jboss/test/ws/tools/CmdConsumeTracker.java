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

import org.jboss.ws.api.tools.WSContractConsumer;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.net.URL;

/**
 * @author Heiko.Braun@jboss.com
 */
public class CmdConsumeTracker extends WSContractConsumer
{
   public static String LAST_EVENT = "";

   @Override
   public void setBindingFiles(List<File> bindingFiles)
   {
      LAST_EVENT += "setBindingFiles";
   }

   @Override
   public void setCatalog(File catalog)
   {
      LAST_EVENT += "setCatalog";
   }

   @Override
   public void setOutputDirectory(File directory)
   {
      LAST_EVENT += "setOutputDirectory";
   }

   @Override
   public void setSourceDirectory(File directory)
   {
      LAST_EVENT += "setSourceDirectory";
   }

   @Override
   public void setGenerateSource(boolean generateSource)
   {
      LAST_EVENT += "setGenerateSource";
   }

   @Override
   public void setTargetPackage(String targetPackage)
   {
      LAST_EVENT += "setTargetPackage";
   }

   @Override
   public void setWsdlLocation(String wsdlLocation)
   {
      LAST_EVENT += "setWsdlLocation";
   }
   
   @Override
   public void setEncoding(String encoding)
   {
      LAST_EVENT += "setEncoding";
   }

   @Override
   public void setMessageStream(PrintStream messageStream)
   {
      LAST_EVENT += "setMessageStream";
   }

   @Override
   public void setAdditionalCompilerClassPath(List<String> classPath)
   {
      LAST_EVENT += "setAdditionalCompilerClassPath";
   }
   
   @Override
   public void setAdditionalHeaders(boolean additionalHeaders)
   {
      LAST_EVENT += "setAdditionalHeaders";
   }

   @Override
   public void setTarget(String target)
   {
      LAST_EVENT += "setTarget";
   }

   @Override
   public void consume(URL wsdl)
   {
      LAST_EVENT += "consume";
   }

   @Override
   public void setExtension(boolean extension)
   {
      LAST_EVENT += "setExtension";
   }

   @Override
   public void setNoCompile(boolean nocompile)
   {
      LAST_EVENT += "setNoCompile";
   }

   @Override
   public void setClientJar(File clientJar)
   {

      LAST_EVENT += "setClientJar";
      
   }
}
