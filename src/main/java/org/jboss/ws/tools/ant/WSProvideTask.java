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

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ExecuteJava;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.CommandlineJava.SysProperties;
import org.jboss.ws.api.tools.WSContractProvider;

import java.io.File;
import java.io.PrintStream;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

/**
 * Ant task which invokes provides a Web Service contract and portable JAX-WS wrapper classes.
 * 
 * <table border="1">
 *   <tr align="left" BGCOLOR="#CCCCFF" CLASS="TableHeadingColor"><th>Attribute</th><th>Description</th><th>Default</th></tr>
 *   <tr><td>fork</td><td>Whether or not to run the generation task in a separate VM.</td><td>true</td></tr>
 *   <tr><td>keep</td><td>Keep/Enable Java source code generation.</td><td>false</td></tr>
 *   <tr><td>destdir</td><td>The output directory for generated artifacts.</td><td>"output"</td></tr>
 *   <tr><td>resourcedestdir</td><td>The output directory for resource artifacts (WSDL/XSD).</td><td>value of destdir</td></tr>
 *   <tr><td>sourcedestdir</td><td>The output directory for Java source.</td><td>value of destdir</td></tr>
 *   <tr><td>genwsdl</td><td>Whether or not to generate WSDL.</td><td>false</td><tr>
 *   <tr><td>address</td><td>The generated port soap:address in wsdl.</td><td></td><tr>
 *   <tr><td>extension</td><td>Enable SOAP 1.2 binding extension.</td><td>false</td></tr>
 *   <tr><td>verbose</td><td>Enables more informational output about cmd progress.</td><td>false</td><tr>
 *   <tr><td>sei</td><td>Service Endpoint Implementation.</td><td></td><tr>
 *   <tr><td>classpath</td><td>The classpath that contains the service endpoint implementation.</td><td>""</tr>
 * </table>
 * <b>* = required.</b>
 * 
 * <p>Example:
 * 
 * <pre>
 *  &lt;target name=&quot;test-wsproivde&quot; depends=&quot;init&quot;&gt;
 *    &lt;taskdef name=&quot;WSProvideTask&quot; classname=&quot;org.jboss.ws.tools.ant.WSProvideTask&quot;&gt;
 *      &lt;classpath refid=&quot;core.classpath&quot;/&gt;
 *    &lt;/taskdef&gt;
 *    &lt;WSProvideTask
 *      fork=&quot;false&quot;
 *      keep=&quot;true&quot;
 *      destdir=&quot;out&quot;
 *      resourcedestdir=&quot;out-resource&quot;
 *      sourcedestdir=&quot;out-source&quot;
 *      genwsdl=&quot;true&quot; 
 *      extension=&quot;true&quot;
 *      verbose=&quot;true&quot;
 *      sei=&quot;org.jboss.test.ws.jaxws.jsr181.soapbinding.DocWrappedServiceImpl&quot;&gt;
 *      &lt;classpath&gt;
 *        &lt;pathelement path=&quot;${tests.output.dir}/classes&quot;/&gt;
 *      &lt;/classpath&gt;
 *    &lt;/WSProvideTask&gt;
 *  &lt;/target&gt;
 * </pre>
 * 
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 */
public class WSProvideTask extends Task
{
   private Path classpath = new Path(getProject());
   private CommandlineJava command = new CommandlineJava();
   private String sei;
   private File destdir;
   private File resourcedestdir;
   private File sourcedestdir;
   private boolean keep;
   private boolean extension;
   private boolean genwsdl;
   private boolean verbose;
   private boolean fork;
   private boolean debug;
   private String address;
   
   // Not actually used right now
   public void setDebug(boolean debug)
   {
      this.debug = debug;
   }
   
   public Commandline.Argument createJvmarg() 
   {
      return command.createVmArgument();
   }
   
   public void setClasspath(Path classpath)
   {
      this.classpath = classpath;
   }
   
   public void setClasspathRef(Reference ref)
   {
      createClasspath().setRefid(ref);
   }
   
   public Path createClasspath()
   {
      return classpath;
   }
   
   public void setDestdir(File destdir)
   {
      this.destdir = destdir;
   }
   
   public void setExtension(boolean extension)
   {
      this.extension = extension;
   }

   public void setProtocol(String protocol)
   {
      if (protocol != null)
      {
         this.extension = protocol.toLowerCase().indexOf("Xsoap1.2") != -1;
      }
   }
   
   public void setKeep(boolean keep)
   {
      this.keep = keep;
   }
   
   public void setSei(String sei)
   {
      this.sei = sei;
   }
   
   public void setFork(boolean fork)
   {
      this.fork = fork;
   }

   public void setResourcedestdir(File resourcedestdir)
   {
      this.resourcedestdir = resourcedestdir;
   }

   public void setSourcedestdir(File sourcedestdir)
   {
      this.sourcedestdir = sourcedestdir;
   }

   public void setVerbose(boolean verbose)
   {
      this.verbose = verbose;
   }

   public void setGenwsdl(boolean genwsdl)
   {
      this.genwsdl = genwsdl;
   }
   
   public void setAddress(String address)
   {
      this.address = address;
   }
   
   private ClassLoader getClasspathLoader(ClassLoader parent)
   {
		AntClassLoader antLoader = new AntClassLoader(parent, getProject(), classpath, false);

		// It's necessary to wrap it into an URLLoader in order to extract that information
		// within the actual provider impl.
		// See SunRIProviderImpl for instance
		List<URL> urls = new ArrayList<URL>();
		StringTokenizer tok = new StringTokenizer(antLoader.getClasspath(), File.separator);
		while(tok.hasMoreTokens())
		{
			try
			{
            String path = tok.nextToken();
            if(!path.startsWith("file://"))
               path = "file://"+path;

            urls.add(new URL(path));
			}
			catch (MalformedURLException e)
			{
				throw new IllegalArgumentException("Failed to wrap classloader", e);
			}

		}

		ClassLoader wrapper = new URLClassLoader(urls.toArray(new URL[0]), antLoader);
		return wrapper;
   }
   
   public void executeNonForked()
   {
      ClassLoader prevCL = SecurityActions.getContextClassLoader();
      ClassLoader antLoader = SecurityActions.getClassLoader(this.getClass());
      SecurityActions.setContextClassLoader(antLoader);
      PrintStream ps = null;
      try
      {
         WSContractProvider gen = WSContractProvider.newInstance(
					getClasspathLoader(antLoader)
			);
         if (verbose) {
            ps = new PrintStream(new LogOutputStream(this, Project.MSG_INFO));
            gen.setMessageStream(ps);
         }
         gen.setGenerateSource(keep);
         gen.setGenerateWsdl(genwsdl);
         gen.setExtension(extension);
         gen.setPortSoapAddress(address);

         if (destdir != null)
            gen.setOutputDirectory(destdir);
         if (resourcedestdir != null)
            gen.setResourceDirectory(resourcedestdir);
         if (sourcedestdir != null)
            gen.setSourceDirectory(sourcedestdir);

         log("Generating from endpoint: " + sei, Project.MSG_INFO);
         
         gen.provide(sei);
      }
      catch(Throwable t)
      {
         throw new BuildException(t, getLocation());  
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
      if (sei == null)
         throw new BuildException("The sei attribute must be specified!", getLocation());
      
      if (fork)
         executeForked();
      else
         executeNonForked();
   }
   
   private Path getTaskClassPath()
   {
      // Why is everything in the Ant API a big hack???
      ClassLoader cl = this.getClass().getClassLoader();
      if (cl instanceof AntClassLoader)
      {
         return new Path(getProject(), ((AntClassLoader)cl).getClasspath());
      }
      
      return new Path(getProject());
   }

   private void executeForked() throws BuildException
   {
      command.setClassname(org.jboss.ws.tools.cmd.WSProvide.class.getName());
      
      Path path = command.createClasspath(getProject());
      path.append(getTaskClassPath());
      path.append(classpath);
     
      if (keep)
         command.createArgument().setValue("-k");
      
      if (genwsdl)
         command.createArgument().setValue("-w");
      
      if (address != null) {
         command.createArgument().setValue("-a");
         command.createArgument().setValue(address);
      }
      
      if (extension)
         command.createArgument().setValue("-e");
      
      if (destdir != null)
      {
         command.createArgument().setValue("-o");
         command.createArgument().setFile(destdir);
      }
      if (resourcedestdir != null)
      {
         command.createArgument().setValue("-r");
         command.createArgument().setFile(resourcedestdir);
      }
      if (sourcedestdir != null)
      {
         command.createArgument().setValue("-s");
         command.createArgument().setFile(sourcedestdir);
      }
      
      if (!verbose)
         command.createArgument().setValue("-q");
      
      // Always dump traces
      command.createArgument().setValue("-t");
      command.createArgument().setValue(sei);
      
      if (verbose)
         log("Command invoked: " + command.getJavaCommand().toString());
      
      CustomExecuteJava execute = new CustomExecuteJava();
      execute.setCommandlineJava(command);
      if (execute.fork(this) != 0)
         throw new BuildException("Could not invoke WSProvideTask", getLocation());
   }
}
