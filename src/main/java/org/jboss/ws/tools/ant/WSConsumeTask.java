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

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.jboss.ws.api.tools.WSContractConsumer;

/**
 * Ant task which consumes a Web Service contract.
 *
 * <table border="1">
 *   <tr align="left" BGCOLOR="#CCCCFF" CLASS="TableHeadingColor"><th>Attribute</th><th>Description</th><th>Default</th></tr>
 *   <tr><td>fork</td><td>Whether or not to run the generation task in a separate VM.</td><td>true</td></tr>
 *   <tr><td>keep</td><td>Keep/Enable Java source code generation.</td><td>false</td></tr>
 *   <tr><td>catalog</td><td> Oasis XML Catalog file for entity resolution</td><td>none</td></tr>
 *   <tr><td>clientjar</td><td>Gnerate the client jar of generated artifacts for calling a webservice</td><td>none</td></tr>
 *   <tr><td>package</td><td> The target Java package for generated code.</td><td>generated</td></tr>
 *   <tr><td>binding</td><td>A JAX-WS or JAXB binding file</td><td>none</td></tr>
 *   <tr><td>wsdlLocation</td><td>Value to use for @@WebService.wsdlLocation</td><td>generated</td></tr>
 *   <tr><td>destdir</td><td>The output directory for generated artifacts.</td><td>"output"</td></tr>
 *   <tr><td>sourcedestdir</td><td>The output directory for Java source.</td><td>value of destdir</td></tr>
 *   <tr><td>extension</td><td>Enable SOAP 1.2 binding extension.</td><td>false</td></tr>
 *   <tr><td>verbose</td><td>Enables more informational output about cmd progress.</td><td>false</td><tr>
 *   <tr><td>wsdl*</td><td>The WSDL file or URL</td><td>n/a</td><tr>
 * </table>
 * <b>* = required.</b>
 *
 * <p>Example:
 *
 * <pre>
 * &lt;WSConsumeTask
 *   fork=&quot;true&quot;
 *   verbose=&quot;true&quot;
 *   destdir=&quot;output&quot;
 *   sourcedestdir=&quot;gen-src&quot;
 *   keep=&quot;true&quot;
 *   wsdllocation=&quot;handEdited.wsdl&quot;
 *   wsdl=&quot;foo.wsdl&quot;&gt;
 *   &lt;binding dir=&quot;binding-files&quot; includes=&quot;*.xml&quot; excludes=&quot;bad.xml&quot;/&gt;
 * &lt;/wsimport&gt;
 * </pre>
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class WSConsumeTask extends Task
{
   private CommandlineJava command = new CommandlineJava();
   private String wsdl;
   private File destdir;
   private File sourcedestdir;
   private List<File> bindingFiles = new ArrayList<File>();
   private File catalog;
   private File clientjar;
   private String wsdlLocation;
   private String encoding;
   private String targetPackage;
   private boolean keep;
   private boolean extension;
   private boolean verbose;
   private boolean fork;
   private boolean debug;
   private boolean nocompile;
   private boolean additionalHeaders;

   // Not actually used right now
   public void setDebug(boolean debug)
   {
      this.debug = debug;
   }

   public Commandline.Argument createJvmarg()
   {
      return command.createVmArgument();
   }

   public void setBinding(File bindingFile)
   {
      bindingFiles.add(bindingFile);
   }

   public void setCatalog(File catalog)
   {
      this.catalog = catalog;
   }
   
   public void setClientJar(File clientJar)
   {
      this.clientjar = clientJar;
   }

   public void setDestdir(File destdir)
   {
      this.destdir = destdir;
   }

   public void setFork(boolean fork)
   {
      this.fork = fork;
   }

   public void setKeep(boolean keep)
   {
      this.keep = keep;
   }

   public void setExtension(boolean extension)
   {
      this.extension = extension;
   }
   
   public void setAdditionalHeaders(boolean additionalHeaders)
   {
      this.additionalHeaders = additionalHeaders;
   }

   public void setSourcedestdir(File sourcedestdir)
   {
      this.sourcedestdir = sourcedestdir;
   }

   public void setPackage(String targetPackage)
   {
      this.targetPackage = targetPackage;
   }

   public void setVerbose(boolean verbose)
   {
      this.verbose = verbose;
   }

   public void setNoCompile(boolean nocompile)
   {
      this.nocompile = nocompile;
   }

   public void setWsdl(String wsdl)
   {
      this.wsdl = wsdl;
   }

   public void setWsdlLocation(String wsdlLocation)
   {
      this.wsdlLocation = wsdlLocation;
   }

   public void setEncoding(String encoding)
   {
      this.encoding = encoding;
   }

   public void addConfiguredBinding(FileSet fs)
   {
      DirectoryScanner ds = fs.getDirectoryScanner(getProject());
      File baseDir = ds.getBasedir();
      for (String file : ds.getIncludedFiles())
      {
         bindingFiles.add(new File(baseDir, file));
      }
   }

   public void executeNonForked()
   {
      ClassLoader prevCL = SecurityActions.getContextClassLoader();
      ClassLoader antLoader = SecurityActions.getClassLoader(this.getClass());
      SecurityActions.setContextClassLoader(antLoader);
      PrintStream ps = null;
      try
      {
         WSContractConsumer consumer = WSContractConsumer.newInstance();
         consumer.setGenerateSource(keep);
         consumer.setExtension(extension);
         consumer.setAdditionalHeaders(additionalHeaders);
         consumer.setNoCompile(nocompile);
         if (destdir != null)
            consumer.setOutputDirectory(destdir);
         if (sourcedestdir != null)
            consumer.setSourceDirectory(sourcedestdir);
         if (targetPackage != null)
            consumer.setTargetPackage(targetPackage);
         if (wsdlLocation != null)
            consumer.setWsdlLocation(wsdlLocation);
         if (clientjar != null)
            consumer.setClientJar(clientjar);
         if (encoding != null)
            consumer.setEncoding(encoding);
         if (catalog != null)
         {
            if (catalog.exists() && catalog.isFile())
            {
               consumer.setCatalog(catalog);
            }
            else
            {
               log("Catalog file not found: " + catalog, Project.MSG_WARN);
            }
         }
         if (bindingFiles != null && bindingFiles.size() > 0)
            consumer.setBindingFiles(bindingFiles);

         log("Consuming wsdl: " + wsdl, Project.MSG_INFO);

         if (verbose)
         {
            ps = new PrintStream(new LogOutputStream(this, Project.MSG_INFO));
            consumer.setMessageStream(ps);
         }

         try
         {
            consumer.setAdditionalCompilerClassPath(getTaskClassPathStrings());
            consumer.consume(wsdl);
         }
         catch (Throwable e)
         {
            throw new BuildException(e, getLocation());
         }
      }
      finally
      {
         if (ps != null) {
            ps.close();
         }
         SecurityActions.setContextClassLoader(prevCL);
      }
   }

   public void execute() throws BuildException
   {
      if (wsdl == null)
         throw new BuildException("The wsdl attribute must be specified!", getLocation());

      if (fork)
         executeForked();
      else executeNonForked();
   }

   private Path getTaskClassPath()
   {
      // Why is everything in the Ant API a big hack???
      ClassLoader cl = SecurityActions.getClassLoader(this.getClass());
      if (cl instanceof AntClassLoader)
      {
         return new Path(getProject(), ((AntClassLoader)cl).getClasspath());
      }

      return new Path(getProject());
   }

   private List<String> getTaskClassPathStrings()
   {
      // Why is everything in the Ant API a big hack???
      List<String> strings = new ArrayList<String>();
      ClassLoader cl = SecurityActions.getClassLoader(this.getClass());
      if (cl instanceof AntClassLoader)
      {
         for (String string : ((AntClassLoader)cl).getClasspath().split(File.pathSeparator))
            strings.add(string);
      }

      return strings;
   }

   private void executeForked() throws BuildException
   {
      command.setClassname(org.jboss.ws.tools.cmd.WSConsume.class.getName());

      Path path = command.createClasspath(getProject());
      path.append(getTaskClassPath());

      if (keep)
         command.createArgument().setValue("-k");
      
      if (extension)
         command.createArgument().setValue("-e");
      
      if (additionalHeaders)
         command.createArgument().setValue("-a");

      for (File file : bindingFiles)
      {
         command.createArgument().setValue("-b");
         command.createArgument().setFile(file);
      }

      if (catalog != null)
      {
         command.createArgument().setValue("-c");
         command.createArgument().setFile(catalog);
      }
      
      if (clientjar != null)
      {
         command.createArgument().setValue("-j");
         command.createArgument().setFile(clientjar);
      }

      if (targetPackage != null)
      {
         command.createArgument().setValue("-p");
         command.createArgument().setValue(targetPackage);
      }

      if (wsdlLocation != null)
      {
         command.createArgument().setValue("-w");
         command.createArgument().setValue(wsdlLocation);
      }
      
      if (encoding != null)
      {
         command.createArgument().setValue("--encoding");
         command.createArgument().setValue(encoding);
      }

      if (destdir != null)
      {
         command.createArgument().setValue("-o");
         command.createArgument().setFile(destdir);
      }

      if (sourcedestdir != null)
      {
         command.createArgument().setValue("-s");
         command.createArgument().setFile(sourcedestdir);
      }

      if (verbose)
         command.createArgument().setValue("-v");

      command.createArgument().setValue(wsdl);

      log("Consuming wsdl: " + wsdl, Project.MSG_INFO);
      
      if (verbose)
         log("Command invoked: " + command.getJavaCommand().toString());

      CustomExecuteJava execute = new CustomExecuteJava();
      execute.setCommandlineJava(command);
      if (execute.fork(this) != 0)
         throw new BuildException("Could not invoke WSConsumeTask", getLocation());
   }
   
}
