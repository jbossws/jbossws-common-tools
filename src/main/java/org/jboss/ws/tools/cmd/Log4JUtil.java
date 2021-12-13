/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.tools.cmd;

import java.net.URISyntaxException;
import java.net.URL;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.util.Loader;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 10-Jun-2010
 *
 */
final class Log4JUtil
{
   public static final String LOG4J_CONFIGURATION = "log4j.configuration";
   public static final String LOG4J_PROPERTIES = "log4j.properties";

   private Log4JUtil()
   {
      // forbidden instantiation
   }
   
   /**
    * Returns true if a log4j configuration can be found given the current environment.
    * See http://logging.apache.org/log4j/1.2/manual.html (Default Initialization Procedure)
    * @return
    */
   public static boolean isLog4jConfigurationAvailable()
   {
      String log4jConfiguration = System.getProperty(LOG4J_CONFIGURATION);
      String resource = log4jConfiguration != null ? log4jConfiguration : LOG4J_PROPERTIES;
      URL url = null;
      try
      {
         url = new URL(resource);
         loadConfiguration(url);
      }
      catch (Exception e1)
      {
         try
         {
            url = Loader.getResource(resource, null);
            loadConfiguration(url);
         }
         catch (Exception e2)
         {
            //ignore
         }
      }
      return url != null;
   }

   private static void loadConfiguration(URL log4jConfigurationFile) throws URISyntaxException {
      if (log4jConfigurationFile == null) {
         return;
      }

      LoggerContext.getContext().setConfigLocation(log4jConfigurationFile.toURI());
   }
}
