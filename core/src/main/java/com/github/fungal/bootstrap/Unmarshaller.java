/*
 * The Fungal kernel project
 * Copyright (C) 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.github.fungal.bootstrap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Unmarshaller for bootstrap.xml
 * @author <a href="mailto:jesper.pedersen@comcast.net">Jesper Pedersen</a>
 */
public class Unmarshaller
{
   /**
    * Constructor
    */
   public Unmarshaller()
   {
   }

   /**
    * Unmarshal
    * @param url The URL
    * @return The result
    * @exception IOException If an I/O error occurs
    */
   public Bootstrap unmarshal(URL url) throws IOException
   {
      if (url == null)
         throw new IllegalArgumentException("URL is null");

      InputStream is = null;
      try
      {
         Bootstrap bootstrap = new Bootstrap();

         if ("file".equals(url.getProtocol()))
         {
            File file = new File(url.toURI());
            is = new FileInputStream(file);
         }
         else if ("jar".equals(url.getProtocol()))
         {
            JarURLConnection jarConnection = (JarURLConnection)url.openConnection();
            is = jarConnection.getInputStream();
         }
         else
         {
            throw new IOException("Unsupport protocol: " + url);
         }

         is = new BufferedInputStream(is, 4096);

         XMLInputFactory xmlInputFactory = null;

         try
         {
            xmlInputFactory = XMLInputFactory.newInstance("javax.xml.stream.XMLInputFactory",
                                                          Thread.currentThread().getContextClassLoader());
         }
         catch (Throwable t)
         {
            xmlInputFactory = XMLInputFactory.newInstance();
         }

         XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);

         while (xmlStreamReader.hasNext())
         {
            int eventCode = xmlStreamReader.next();

            switch (eventCode)
            {
               case XMLStreamReader.START_ELEMENT :

                  if ("url".equals(xmlStreamReader.getLocalName()))
                  {
                     bootstrap.getUrl().add(readUrl(xmlStreamReader));
                  }
                  else if ("servers".equals(xmlStreamReader.getLocalName()))
                  {
                     bootstrap.setServers(readServers(xmlStreamReader));
                  }
                  else if ("dependencies".equals(xmlStreamReader.getLocalName()))
                  {
                     bootstrap.setDependencies(readDependencies(xmlStreamReader));
                  }

                  break;
               default :
            }
         }

         return bootstrap;
      }
      catch (Throwable t)
      {
         throw new IOException(t.getMessage(), t);
      }
      finally
      {
         try
         {
            if (is != null)
               is.close();
         }
         catch (IOException ioe)
         {
            // Ignore
         }
      }
   }

   /**
    * Read: <url>
    * @param xmlStreamReader The XML stream
    * @return The result
    * @exception XMLStreamException Thrown if an error occurs
    */
   private String readUrl(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      String result = null;

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.CHARACTERS :
               result = xmlStreamReader.getText();
               break;
            default : 
         }
         eventCode = xmlStreamReader.next();
      }

      if (!"url".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("url tag not completed");

      return result;
   }

   /**
    * Read: <servers>
    * @param xmlStreamReader The XML stream
    * @return The result
    * @exception XMLStreamException Thrown if an error occurs
    */
   private ServersType readServers(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      ServersType result = new ServersType();

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.START_ELEMENT :
               String name = xmlStreamReader.getLocalName();

               if ("server".equals(name))
                  result.getServer().add(readServer(xmlStreamReader));

               break;
            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"servers".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("servers tag not completed");

      return result;
   }

   /**
    * Read: <server>
    * @param xmlStreamReader The XML stream
    * @return The result
    * @exception XMLStreamException Thrown if an error occurs
    */
   private String readServer(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      String result = null;

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.CHARACTERS :
               if (!xmlStreamReader.getText().trim().equals(""))
                  result = xmlStreamReader.getText().trim();

               break;

            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"server".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("server tag not completed");

      return result;
   }

   /**
    * Read: <dependencies>
    * @param xmlStreamReader The XML stream
    * @return The result
    * @exception XMLStreamException Thrown if an error occurs
    */
   private DependenciesType readDependencies(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      DependenciesType result = new DependenciesType();

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.START_ELEMENT :
               String name = xmlStreamReader.getLocalName();

               if ("dependency".equals(name))
                  result.getDependency().add(readDependency(xmlStreamReader));

               break;
            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"dependencies".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("dependencies tag not completed");

      return result;
   }

   /**
    * Read: <dependency>
    * @param xmlStreamReader The XML stream
    * @return The result
    * @exception XMLStreamException Thrown if an error occurs
    */
   private DependencyType readDependency(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      DependencyType result = new DependencyType();

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.START_ELEMENT :
               String name = xmlStreamReader.getLocalName();
               if ("groupId".equals(name))
               {
                  result.setGroupId(readString(xmlStreamReader));
               }
               else if ("artifactId".equals(name))
               {
                  result.setArtifactId(readString(xmlStreamReader));
               }
               else if ("version".equals(name))
               {
                  result.setVersion(readString(xmlStreamReader));
               }
               else if ("type".equals(name))
               {
                  result.setType(readString(xmlStreamReader));
               }

               break;
            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"dependency".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("dependency tag not completed");

      return result;
   }

   /**
    * Read a string
    * @param xmlStreamReader The XML stream
    * @return The parameter
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private String readString(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      String result = null;

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.CHARACTERS :
               if (!xmlStreamReader.getText().trim().equals(""))
                  result = xmlStreamReader.getText().trim();

               break;

            default :
         }

         eventCode = xmlStreamReader.next();
      }

      return result;
   }
}