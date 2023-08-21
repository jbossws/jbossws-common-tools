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

import org.jboss.ws.tools.cmd.WSProvide;

/**
 * @author Heiko.Braun@jboss.com
 */
public class CmdProvideTestCase extends CommandlineTestBase
{

   protected void setUp() throws Exception
   {
      super.setUp();

      // clear events
      CmdProvideTracker.LAST_EVENT = "";

      // enforce loading of the tracker implemenation
      System.setProperty(
        "org.jboss.ws.api.tools.ProviderFactory",
        "org.jboss.test.ws.tools.CmdProvideTrackerFactory"
      );
   }

   /** <pre>
 *  usage: WSProvideTask [options] &lt;endpoint class name&gt;
 *  options:
 *  -h, --help                  Show this help message
 *  -k, --keep                  Keep/Generate Java source
 *  -w, --wsdl                  Enable WSDL file generation
 *  -c, --classpath=&lt;path&lt;      The classpath that contains the endpoint
 *  -o, --output=&lt;directory&gt;    The directory to put generated artifacts
 *  -r, --resource=&lt;directory&gt;  The directory to put resource artifacts
 *  -s, --source=&lt;directory&gt;    The directory to put Java source
 *  -q, --quiet                 Be somewhat more quiet
 *  -t, --show-traces           Show full exception stack traces
 *  -l, --load-provider           Load the provider and exit (debug utility)
 * </pre>
    * */

   public void testMissingOptions() throws Exception
   {
      executeCmd(null, true);   
   }

   public void testValidOutputDir() throws Exception
   {
      executeCmd("-o outputDir org.jboss.test.ws.tools.CalculatorBean", false);
      assertTrue("setOutputDirectory() not invoked", CmdProvideTracker.LAST_EVENT.indexOf("setOutputDirectory")!=-1);
   }

   void runDelegate(String[] args) throws Exception
   {
      WSProvide.main(args);
   }
}
