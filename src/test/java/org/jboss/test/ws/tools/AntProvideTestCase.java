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

/**
 * @author Heiko.Braun@jboss.com
 */
public class AntProvideTestCase extends BuildFileTest
{
   protected void setUp() throws Exception
   {
      super.setUp();

      // cleanup events
      CmdProvideTracker.LAST_EVENT = "";

      // enforce loading of the tracker implemenation
      System.setProperty("org.jboss.ws.api.tools.ProviderFactory", "org.jboss.test.ws.tools.CmdProvideTrackerFactory");

      configureProject("src/test/resources/smoke/tools/provide-test.xml");
   }

   public void testPlainInvocation()
   {
      executeTarget("plainInvocation");
      assertTrue("provide() not invoked", CmdProvideTracker.LAST_EVENT.indexOf("provide") != -1);
   }

   public void testIncludeWSDL()
   {
      executeTarget("includeWSDL");
      assertTrue("setGenerateWsdl() not invoked", CmdProvideTracker.LAST_EVENT.indexOf("setGenerateWsdl") != -1);
   }

   public void testExtraClasspath()
   {
      executeTarget("extraClasspath");

   }

}
