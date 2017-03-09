/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
