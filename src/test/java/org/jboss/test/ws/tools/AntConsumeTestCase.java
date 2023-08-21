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
 * Test the WSConsumeTask.
 * This test needs to be executed in 'SPI_HOME/output/tests',
 * because it works with relative paths.
 * 
 * @author Heiko.Braun@jboss.com
 */
public class AntConsumeTestCase extends BuildFileTest
{
   protected void setUp() throws Exception
   {
      super.setUp();

      // cleanup events
      CmdConsumeTracker.LAST_EVENT = "";

      // enforce loading of the tracker implemenation
      System.setProperty("org.jboss.ws.api.tools.ConsumerFactory", "org.jboss.test.ws.tools.CmdConsumeTrackerFactory");

      configureProject("src/test/resources/smoke/tools/consume-test.xml");
   }

   public void testPlainInvocation()
   {
      executeTarget("plainInvocation");
      assertTrue("consume() not invoked", CmdConsumeTracker.LAST_EVENT.indexOf("consume") != -1);
   }

}
