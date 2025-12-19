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
package org.jboss.ws.tools.cmd;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.jboss.ws.api.tools.WSContractProvider;
import org.jboss.ws.tools.ExitHandler;
import org.jboss.ws.tools.ExitHandlerFactory;
import org.jboss.ws.tools.SystemExitHandler;
import org.jboss.ws.tools.SystemExitHandlerFactory;

/**
 * WSProvideTask is a cmd line tool that generates portable JAX-WS artifacts
 * for a service endpoint implementation.
 * 
 * <pre>
 *  usage: WSProvideTask [options] &lt;endpoint class name&gt;
 *  options: 
 *  <table>
 *  <tr><td>-h, --help                      </td><td>Show this help message</td></tr>
 *  <tr><td>-k, --keep                      </td><td>Keep/Generate Java source</td></tr>
 *  <tr><td>-w, --wsdl                      </td><td>Enable WSDL file generation</td></tr>
 *  <tr><td>-c, --classpath=&lt;path&lt;    </td><td>The classpath that contains the endpoint</td></tr>
 *  <tr><td>-o, --output=&lt;directory&gt;  </td><td>The directory to put generated artifacts</td></tr>
 *  <tr><td>-r, --resource=&lt;directory&gt;</td><td>The directory to put resource artifacts</td></tr>
 *  <tr><td>-s, --source=&lt;directory&gt;  </td><td>The directory to put Java source</td></tr>
 *  <tr><td>-a, --address=&lt;address&gt;   </td><td>The generated port soap:address in wsdl</td></tr>
 *  <tr><td>-q, --quiet                     </td><td>Be somewhat more quiet</td></tr>
 *  <tr><td>-t, --show-traces               </td><td>Show full exception stack traces</td></tr>
 *  <tr><td>-v, --verbose                   </td><td>Show full exception stack traces</td></tr>
 *  <tr><td>-l, --load-provider             </td><td>Load the provider and exit (debug utility)</td></tr>
 *  <tr><td>-e, --extension                 </td><td>Enable SOAP 1.2 binding extension</td></tr>
 * </pre>
 * 
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 */
public class WSProvide
{
   private ClassLoader loader = WSProvide.class.getClassLoader();
   private File outputDir = new File("output");
   private boolean generateSource;
   private boolean generateWsdl;
   private boolean extension;
   private boolean quiet;
   private boolean showTraces;
   private boolean verbose;
   private boolean loadProvider;
   private File resourceDir;
   private File sourceDir;
   private String portSoapAddress;

   private final ExitHandler exitHandler;

   public static final String PROGRAM_NAME = SecurityActions.getSystemProperty("program.name", WSProvide.class.getSimpleName());

   /**
    * Provides an exit handler implementation, based on whether the command is to be used by production or test code.
    * The production code {@link SystemExitHandlerFactory}, which is the default, provides a {@link SystemExitHandler},
    * that calls a pure {@code System.exit()}.<br>
    * Other factories provide {@link ExitHandler} implementations that can override this behavior, for example
    * to throw an exception instead of calling {@code System.exit()}.
    * <p>
    *     Thread safe as {@link ThreadLocal} holds the factory for each thread.
    *     Each thread gets its own reference to the factory, preventing interference in parallel tests.
    * </p>
    */
   private static final ThreadLocal<ExitHandlerFactory> factoryThreadLocal =
           ThreadLocal.withInitial(SystemExitHandlerFactory::getInstance);

   /**
    * Tests call this BEFORE calling main() to inject TestExitHandlerFactory.
    * Thread-safe: only affects the current thread.
    *
    * @param factory The concrete {@link ExitHandlerFactory} instance to use
    */
   public static void setExitHandlerFactory(ExitHandlerFactory factory)
   {
      factoryThreadLocal.set(factory);
   }

   /**
    * Reset to default factory.
    * Tests should call this in finally blocks to clean up.
    */
   public static void resetExitHandlerFactory()
   {
      // Revert to default
      factoryThreadLocal.remove();
   }

   public static void main(String[] args)
   {
      // Get the factory for THIS thread (singleton instance)
      ExitHandlerFactory factory = factoryThreadLocal.get();

      // Gets the concrete exit handler using the factory (also singleton instance)
      WSProvide generate = new WSProvide(factory.get());
      String endpoint = generate.parseArguments(args);
      generate.exitHandler.exit(generate.generate(endpoint));
   }

   WSProvide(ExitHandler exitHandler) {
      this.exitHandler = exitHandler;
   }

   private String parseArguments(String[] args)
   {
      String shortOpts = "hwko:r:s:a:c:qtle";
      LongOpt[] longOpts = 
      {
         new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
         new LongOpt("wsdl", LongOpt.NO_ARGUMENT, null, 'w'),
         new LongOpt("keep", LongOpt.NO_ARGUMENT, null, 'k'),
         new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o'),
         new LongOpt("resource", LongOpt.REQUIRED_ARGUMENT, null, 'r'),
         new LongOpt("source", LongOpt.REQUIRED_ARGUMENT, null, 's'),
         new LongOpt("address", LongOpt.REQUIRED_ARGUMENT, null, 'a'),
         new LongOpt("classpath", LongOpt.REQUIRED_ARGUMENT, null, 'c'),
         new LongOpt("quiet", LongOpt.NO_ARGUMENT, null, 'q'),
         new LongOpt("show-traces", LongOpt.NO_ARGUMENT, null, 't'),
         new LongOpt("verbose", LongOpt.NO_ARGUMENT, null, 'v'),
         new LongOpt("load-provider", LongOpt.NO_ARGUMENT, null, 'l'),
         new LongOpt("extension", LongOpt.NO_ARGUMENT, null, 'e'),
      };
      
      Getopt getopt = new Getopt(PROGRAM_NAME, args, shortOpts, longOpts);
      int c;
      while ((c = getopt.getopt()) != -1)
      {
         switch (c)
         {
            case 'k':
               generateSource = true;
               break;
            case 's':
               sourceDir = new File(getopt.getOptarg());
               break;
            case 'r':
               resourceDir = new File(getopt.getOptarg());
               break;
            case 'w':
               generateWsdl = true;
               break;
            case 't':
               showTraces = true;
               break;
            case 'v':
               verbose = true;
               break;
            case 'o':
               outputDir = new File(getopt.getOptarg());
               break;
            case 'q':
               quiet = true;
               break;
            case 'c':
               processClassPath(getopt.getOptarg());
               break;
            case 'a':
               portSoapAddress = getopt.getOptarg();
               break;
            case 'l':
               loadProvider = true;
               break;
            case 'e':
               extension = true;
               break;
            case 'h':
               printHelp();
               exitHandler.exit(0);
            case '?':
               exitHandler.exit(1);
         }
      }

      // debug output
      if(loadProvider)
      {
         WSContractProvider gen = WSContractProvider.newInstance(loader);
         System.out.println("WSContractProvider instance: " + gen.getClass().getCanonicalName());
         exitHandler.exit(0);
      }

      int endpointPos = getopt.getOptind();
      if (endpointPos >= args.length)
      {
         System.err.println("Error: endpoint implementation was not specified!");
         printHelp();
         exitHandler.exit(1);
      }
      
      return args[endpointPos];
   }
   
   
   private int generate(String endpoint)
   {
      try
      {
         SecurityActions.loadClass(loader, endpoint);
      }
      catch (Exception e)
      {
         System.err.println("Error: Could not load class [" + endpoint + "]. Did you specify a valid --classpath?");
         return 1;
      }
      
      WSContractProvider gen = WSContractProvider.newInstance(loader);
      gen.setGenerateWsdl(generateWsdl);
      gen.setGenerateSource(generateSource);
      gen.setOutputDirectory(outputDir);
      gen.setExtension(extension);
      gen.setPortSoapAddress(portSoapAddress);
      if (resourceDir != null)
         gen.setResourceDirectory(resourceDir);
      if (sourceDir != null)
         gen.setSourceDirectory(sourceDir);

      PrintStream ps = System.out;
      if (! quiet)
      {
         gen.setMessageStream(ps);
      }
      
      try
      {
         gen.provide(endpoint);
         return 0;
      }
      catch (Throwable t)
      {
         System.err.println("Error: Could not generate. (use --show-traces or --verbose to see full traces)");
         if (!showTraces && !verbose)
         {
            String message = t.getMessage();
            if (message == null)
               message = t.getClass().getSimpleName();
            System.err.println("Error: " + message);
         }
         else
         {
            t.printStackTrace(System.err);
         }
      }
      
      return 1;
   }

   private void processClassPath(String classPath)
   {
      String[] entries =  classPath.split(File.pathSeparator);
      List<URL> urls= new ArrayList<URL>(entries.length);
      for (String entry : entries)
      {
         try 
         {
            urls.add(new File(entry).toURI().toURL());
         }
         catch (MalformedURLException e)
         {
            System.err.println("Error: a classpath entry was malformed: " + entry);
         }
      }
      loader = new URLClassLoader(urls.toArray(new URL[0]), loader);
   }

   private static void printHelp()
   {
      PrintStream out = System.out;
      out.println("WSProvideTask generates portable JAX-WS artifacts for an endpoint implementation.\n");
      out.println("usage: " + PROGRAM_NAME + " [options] <endpoint class name>\n");
      out.println("options: ");
      out.println("    -h, --help                  Show this help message");
      out.println("    -k, --keep                  Keep/Generate Java source");
      out.println("    -w, --wsdl                  Enable WSDL file generation");
      out.println("    -a, --address=<address>     The generated port soap:address in wsdl");
      out.println("    -c, --classpath=<path>      The classpath that contains the endpoint");
      out.println("    -o, --output=<directory>    The directory to put generated artifacts");
      out.println("    -r, --resource=<directory>  The directory to put resource artifacts");
      out.println("    -s, --source=<directory>    The directory to put Java source");
      out.println("    -e, --extension             Enable SOAP 1.2 binding extension");
      out.println("    -q, --quiet                 Be somewhat more quiet");
      out.println("    -t, --show-traces           Show full exception stack traces");
      out.println("    -v, --verbose               Show full exception stack traces");
      out.println("    -l, --load-provider         Load the provider and exit (debug utility)");
      out.flush();
   }
}
