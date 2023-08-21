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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * An Ant task for installing AS7 module.xml files; reads the contents of the provided
 * module.xml files, computes the module path from their full paths, updates the contents
 * with the resource libs found at the corresponding module in the target dir location
 * and finally write the destination module.xml files in the target module dir.
 * 
 * @author alessio.soldano@jboss.com
 * @since 03-Feb-2011
 *
 */
public class InstallModulesTask extends Task
{
   private String targetDir; //the target directory where modules are installed
   private FileSet fileset; //the fileset of module.xml files to be processed 

   @Override
   public void execute() throws BuildException
   {
      try
      {
         DirectoryScanner dsc = fileset.getDirectoryScanner(getProject());
         File baseDir = dsc.getBasedir();
         String[] files = dsc.getIncludedFiles();
         for (int i = 0; i < files.length; i++)
         {
            String currentFile = files[i];
            File moduleXml = new File(baseDir, currentFile);
            String modulePath = currentFile.substring(0, currentFile.lastIndexOf(File.separator));
            File libDir = new File(targetDir, modulePath);
            File destFile = new File(targetDir, currentFile);
            System.out.println("Processing descriptor for module " + modulePath);
            System.out.println("* Source module descriptor: " + moduleXml);
            System.out.println("* Destination module descriptor: " + destFile);
            String c = readFileContents(moduleXml);
            if (libDir.exists())
            {
               String[] libs = libDir.list(new FilenameFilter()
               {
                  @Override
                  public boolean accept(File dir, String name)
                  {
                     return name.endsWith(".jar") || name.endsWith(".ear") || name.endsWith(".sar")
                           || name.endsWith(".war");
                  }
               });
               BufferedWriter out = new BufferedWriter(new FileWriter(destFile));
               out.write(updateContents(c, libs));
               out.close();
            }
            else
            {
               libDir.mkdirs();
               BufferedWriter out = new BufferedWriter(new FileWriter(destFile));
               out.write(c);
               out.close();
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new BuildException(e);
      }
   }
   
   private static String readFileContents(File file) throws Exception
   {
      StringBuilder sb = new StringBuilder();
      BufferedReader in = null;
      try
      {
         in = new BufferedReader(new FileReader(file));
         String line;
         while ((line = in.readLine()) != null)
         {
            sb.append(line);
            sb.append("\n");
         }
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (Exception e)
            {
               //ignore
            }
         }
      }
      return sb.toString();
   }
   
   private static String updateContents(String contents, String[] libs)
   {
      StringBuilder sb = new StringBuilder();
      for (String f : libs) {
         sb.append("<resource-root path=\"");
         sb.append(f);
         sb.append("\"/>\n        ");
      }
      return contents.replaceFirst("<!-- Insert resources here -->", sb.toString());
   }

   public FileSet createFileset()
   {
      this.fileset = new FileSet();
      return fileset;
   }

   public void setTargetDir(String targetDir)
   {
      this.targetDir = targetDir;
   }
}
