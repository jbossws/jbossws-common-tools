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

import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteJava;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.taskdefs.Redirector;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.util.TimeoutObserver;
import org.apache.tools.ant.util.Watchdog;

public class CustomExecuteJava implements TimeoutObserver
{
   private CommandlineJava command = null;
   private Long timeout = null;
   private volatile boolean timedOut = false;
   private Thread thread = null;

   public void setCommandlineJava(CommandlineJava command) {
       this.command = command;
   }

   public void setTimeout(Long timeout) {
       this.timeout = timeout;
   }

   public synchronized void timeoutOccured(Watchdog w) {
       if (thread != null) {
           timedOut = true;
           thread.interrupt();
       }
       notifyAll();
   }

   public synchronized boolean killedProcess() {
       return timedOut;
   }

   public int fork(ProjectComponent pc) throws BuildException {
       Redirector redirector = new Redirector(pc);
       Execute exe
           = new Execute(redirector.createHandler(),
                         timeout == null
                         ? null
                         : new ExecuteWatchdog(timeout.longValue()));
       exe.setAntRun(pc.getProject());
       
       String[] cl = command.getCommandline();
       for (int i = 0; i < cl.length; i++) {
          if (cl[i].endsWith("\n")) {
             cl[i] = cl[i].substring(0, cl[i].length() - 1);
          }
       }
       
       if (Os.isFamily("openvms")) {
           ExecuteJava.setupCommandLineForVMS(exe, cl);
       } else {
           exe.setCommandline(cl);
       }
       try {
           int rc = exe.execute();
           redirector.complete();
           return rc;
       } catch (IOException e) {
           throw new BuildException(e);
       } finally {
           timedOut = exe.killedProcess();
       }
   }

}
