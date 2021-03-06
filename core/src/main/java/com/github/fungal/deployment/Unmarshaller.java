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

package com.github.fungal.deployment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Unmarshaller for a bean deployment XML file
 * @author <a href="mailto:jesper.pedersen@comcast.net">Jesper Pedersen</a>
 */
public class Unmarshaller
{
   /** The logger */
   private Logger log = Logger.getLogger(Unmarshaller.class.getName());

   /** Trace logging enabled */
   private boolean trace = log.isLoggable(Level.FINEST);

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
   public Deployment unmarshal(URL url) throws IOException
   {
      if (url == null)
         throw new IllegalArgumentException("File is null");

      InputStream is = null;
      try
      {
         Deployment deployment = new Deployment();

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

                  if ("bean".equals(xmlStreamReader.getLocalName()))
                     deployment.getBean().add(readBean(xmlStreamReader));

                  break;
               default :
            }
         }

         return deployment;
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
    * Read: <bean>
    * @param xmlStreamReader The XML stream
    * @return The bean
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private BeanType readBean(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      BeanType result = new BeanType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("name".equals(name))
         {
            result.setName(xmlStreamReader.getAttributeValue(i));
         }
         else if ("class".equals(name))
         {
            result.setClazz(xmlStreamReader.getAttributeValue(i));
         }
         else if ("interface".equals(name))
         {
            result.setInterface(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.START_ELEMENT :
               String name = xmlStreamReader.getLocalName();
               if ("constructor".equals(name))
               {
                  result.setConstructor(readConstructor(xmlStreamReader));
               }
               else if ("property".equals(name))
               {
                  result.getProperty().add(readProperty(xmlStreamReader));
               }
               else if ("depends".equals(name))
               {
                  result.getDepends().add(readDepends(xmlStreamReader));
               }
               else if ("install".equals(name))
               {
                  result.getInstall().add(readInstall(xmlStreamReader));
               }
               else if ("uninstall".equals(name))
               {
                  result.getUninstall().add(readUninstall(xmlStreamReader));
               }
               else if ("incallback".equals(name))
               {
                  result.getIncallback().add(readIncallback(xmlStreamReader));
               }
               else if ("uncallback".equals(name))
               {
                  result.getUncallback().add(readUncallback(xmlStreamReader));
               }
               else if ("create".equals(name))
               {
                  result.setCreate(readCreate(xmlStreamReader));
               }
               else if ("start".equals(name))
               {
                  result.setStart(readStart(xmlStreamReader));
               }
               else if ("stop".equals(name))
               {
                  result.setStop(readStop(xmlStreamReader));
               }
               else if ("destroy".equals(name))
               {
                  result.setDestroy(readDestroy(xmlStreamReader));
               }
               else if ("ignoreCreate".equals(name))
               {
                  result.setIgnoreCreate(readIgnoreCreate(xmlStreamReader));
               }
               else if ("ignoreStart".equals(name))
               {
                  result.setIgnoreStart(readIgnoreStart(xmlStreamReader));
               }
               else if ("ignoreStop".equals(name))
               {
                  result.setIgnoreStop(readIgnoreStop(xmlStreamReader));
               }
               else if ("ignoreDestroy".equals(name))
               {
                  result.setIgnoreDestroy(readIgnoreDestroy(xmlStreamReader));
               }

               break;
            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"bean".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("bean tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <constructor>
    * @param xmlStreamReader The XML stream
    * @return The constructor
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private ConstructorType readConstructor(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      ConstructorType result = new ConstructorType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("factoryMethod".equals(name))
         {
            result.setFactoryMethod(xmlStreamReader.getAttributeValue(i));
         }
         else if ("factoryClass".equals(name))
         {
            result.setFactoryClass(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.START_ELEMENT :
               String name = xmlStreamReader.getLocalName();

               if ("parameter".equals(name))
               {
                  result.getParameter().add(readParameter(xmlStreamReader));
               }
               else if ("factory".equals(name))
               {
                  result.setFactory(readFactory(xmlStreamReader));
               }

               break;
            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"constructor".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("constructor tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <parameter>
    * @param xmlStreamReader The XML stream
    * @return The parameter
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private ParameterType readParameter(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      ParameterType result = new ParameterType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("class".equals(name))
         {
            result.setClazz(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.START_ELEMENT :
               String name = xmlStreamReader.getLocalName();

               if ("inject".equals(name))
               {
                  result.getContent().add(readInject(xmlStreamReader));
               }
               else if ("null".equals(name))
               {
                  result.getContent().add(readNull(xmlStreamReader));
               }

               break;

            case XMLStreamReader.CHARACTERS :
               if (!xmlStreamReader.getText().trim().equals(""))
                  result.getContent().add(xmlStreamReader.getText());

               break;

            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"parameter".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("parameter tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <property>
    * @param xmlStreamReader The XML stream
    * @return The property
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private PropertyType readProperty(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      PropertyType result = new PropertyType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("name".equals(name))
         {
            result.setName(xmlStreamReader.getAttributeValue(i));
         }
         else if ("class".equals(name))
         {
            result.setClazz(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.START_ELEMENT :
               String name = xmlStreamReader.getLocalName();

               if ("inject".equals(name))
               {
                  result.getContent().add(readInject(xmlStreamReader));
               }
               else if ("set".equals(name))
               {
                  result.getContent().add(readSet(xmlStreamReader));
               }
               else if ("map".equals(name))
               {
                  result.getContent().add(readMap(xmlStreamReader));
               }
               else if ("list".equals(name))
               {
                  result.getContent().add(readList(xmlStreamReader));
               }
               else if ("null".equals(name))
               {
                  result.getContent().add(readNull(xmlStreamReader));
               }
               else if ("this".equals(name))
               {
                  result.getContent().add(readThis(xmlStreamReader));
               }

               break;

            case XMLStreamReader.CHARACTERS :
               if (!xmlStreamReader.getText().trim().equals(""))
                  result.getContent().add(xmlStreamReader.getText());

               break;

            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"property".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("property tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <inject>
    * @param xmlStreamReader The XML stream
    * @return The inject
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private InjectType readInject(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      InjectType result = new InjectType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("bean".equals(name))
         {
            result.setBean(xmlStreamReader.getAttributeValue(i));
         }
         else if ("property".equals(name))
         {
            result.setProperty(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.CHARACTERS :
               result.setValue(xmlStreamReader.getText());
               break;

            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"inject".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("inject tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <depends>
    * @param xmlStreamReader The XML stream
    * @return The depends
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private DependsType readDepends(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      DependsType result = new DependsType();

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.CHARACTERS :
               result.setValue(xmlStreamReader.getText());
               break;

            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"depends".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("depends tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <install>
    * @param xmlStreamReader The XML stream
    * @return The install
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private InstallType readInstall(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      InstallType result = new InstallType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("method".equals(name))
         {
            result.setMethod(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         eventCode = xmlStreamReader.next();
      }

      if (!"install".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("install tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <uninstall>
    * @param xmlStreamReader The XML stream
    * @return The install
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private UninstallType readUninstall(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      UninstallType result = new UninstallType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("method".equals(name))
         {
            result.setMethod(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         eventCode = xmlStreamReader.next();
      }

      if (!"uninstall".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("uninstall tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <incallback>
    * @param xmlStreamReader The XML stream
    * @return The incallback
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private IncallbackType readIncallback(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      IncallbackType result = new IncallbackType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("method".equals(name))
         {
            result.setMethod(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         eventCode = xmlStreamReader.next();
      }

      if (!"incallback".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("incallback tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <uncallback>
    * @param xmlStreamReader The XML stream
    * @return The uncallback
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private UncallbackType readUncallback(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      UncallbackType result = new UncallbackType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("method".equals(name))
         {
            result.setMethod(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         eventCode = xmlStreamReader.next();
      }

      if (!"uncallback".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("uncallback tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <map>
    * @param xmlStreamReader The XML stream
    * @return The map
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private MapType readMap(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      MapType result = new MapType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("keyClass".equals(name))
         {
            result.setKeyClass(xmlStreamReader.getAttributeValue(i));
         }
         else if ("valueClass".equals(name))
         {
            result.setValueClass(xmlStreamReader.getAttributeValue(i));
         }
         else if ("class".equals(name))
         {
            result.setClazz(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.START_ELEMENT :
               String name = xmlStreamReader.getLocalName();

               if ("entry".equals(name))
                  result.getEntry().add(readEntry(xmlStreamReader));

               break;

            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"map".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("map tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <set>
    * @param xmlStreamReader The XML stream
    * @return The set
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private SetType readSet(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      SetType result = new SetType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("elementClass".equals(name))
         {
            result.setElementClass(xmlStreamReader.getAttributeValue(i));
         }
         else if ("class".equals(name))
         {
            result.setClazz(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.START_ELEMENT :
               String name = xmlStreamReader.getLocalName();

               if ("value".equals(name))
                  result.getValue().add(readValue(xmlStreamReader));

               break;

            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"set".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("set tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <list>
    * @param xmlStreamReader The XML stream
    * @return The list
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private ListType readList(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      ListType result = new ListType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("elementClass".equals(name))
         {
            result.setElementClass(xmlStreamReader.getAttributeValue(i));
         }
         else if ("class".equals(name))
         {
            result.setClazz(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.START_ELEMENT :
               String name = xmlStreamReader.getLocalName();

               if ("value".equals(name))
                  result.getValue().add(readValue(xmlStreamReader));

               break;

            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"list".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("list tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <entry>
    * @param xmlStreamReader The XML stream
    * @return The entry
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private EntryType readEntry(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      EntryType result = new EntryType();

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.START_ELEMENT :
               String name = xmlStreamReader.getLocalName();

               if ("key".equals(name))
               {
                  result.setKey(readKey(xmlStreamReader));
               }
               else if ("value".equals(name))
               {
                  result.setValue(readValue(xmlStreamReader));
               }

               break;

            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"entry".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("entry tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <key>
    * @param xmlStreamReader The XML stream
    * @return The key
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private KeyType readKey(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      KeyType result = new KeyType();

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.CHARACTERS :
               result.setValue(xmlStreamReader.getText());
               break;

            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"key".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("key tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <value>
    * @param xmlStreamReader The XML stream
    * @return The value
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private ValueType readValue(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      ValueType result = new ValueType();

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.CHARACTERS :
               result.setValue(xmlStreamReader.getText());
               break;

            default :
         }

         eventCode = xmlStreamReader.next();
      }

      if (!"value".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("value tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <null>
    * @param xmlStreamReader The XML stream
    * @return The null
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private NullType readNull(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      NullType result = new NullType();

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         eventCode = xmlStreamReader.next();
      }

      if (!"null".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("null tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <this>
    * @param xmlStreamReader The XML stream
    * @return The this
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private ThisType readThis(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      ThisType result = new ThisType();

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         eventCode = xmlStreamReader.next();
      }

      if (!"this".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("this tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <factory>
    * @param xmlStreamReader The XML stream
    * @return The factory
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private FactoryType readFactory(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      FactoryType result = new FactoryType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("bean".equals(name))
         {
            result.setBean(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         eventCode = xmlStreamReader.next();
      }

      if (!"factory".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("factory tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <create>
    * @param xmlStreamReader The XML stream
    * @return The create
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private CreateType readCreate(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      CreateType result = new CreateType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("method".equals(name))
         {
            result.setMethod(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         eventCode = xmlStreamReader.next();
      }

      if (!"create".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("create tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <start>
    * @param xmlStreamReader The XML stream
    * @return The start
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private StartType readStart(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      StartType result = new StartType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("method".equals(name))
         {
            result.setMethod(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         eventCode = xmlStreamReader.next();
      }

      if (!"start".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("start tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <stop>
    * @param xmlStreamReader The XML stream
    * @return The stop
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private StopType readStop(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      StopType result = new StopType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("method".equals(name))
         {
            result.setMethod(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         eventCode = xmlStreamReader.next();
      }

      if (!"stop".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("stop tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <destroy>
    * @param xmlStreamReader The XML stream
    * @return The destroy
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private DestroyType readDestroy(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      DestroyType result = new DestroyType();

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("method".equals(name))
         {
            result.setMethod(xmlStreamReader.getAttributeValue(i));
         }
      }

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         eventCode = xmlStreamReader.next();
      }

      if (!"destroy".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("destroy tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <ignoreCreate>
    * @param xmlStreamReader The XML stream
    * @return The ignoreCreate
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private IgnoreCreateType readIgnoreCreate(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      IgnoreCreateType result = new IgnoreCreateType();

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         eventCode = xmlStreamReader.next();
      }

      if (!"ignoreCreate".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("ignoreCreate tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <ignoreStart>
    * @param xmlStreamReader The XML stream
    * @return The ignoreStart
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private IgnoreStartType readIgnoreStart(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      IgnoreStartType result = new IgnoreStartType();

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         eventCode = xmlStreamReader.next();
      }

      if (!"ignoreStart".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("ignoreStart tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <ignoreStop>
    * @param xmlStreamReader The XML stream
    * @return The ignoreStop
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private IgnoreStopType readIgnoreStop(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      IgnoreStopType result = new IgnoreStopType();

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         eventCode = xmlStreamReader.next();
      }

      if (!"ignoreStop".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("ignoreStop tag not completed", xmlStreamReader.getLocation());

      return result;
   }

   /**
    * Read: <ignoreDestroy>
    * @param xmlStreamReader The XML stream
    * @return The ignoreDestroy
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private IgnoreDestroyType readIgnoreDestroy(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      IgnoreDestroyType result = new IgnoreDestroyType();

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         eventCode = xmlStreamReader.next();
      }

      if (!"ignoreDestroy".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("ignoreDestroy tag not completed", xmlStreamReader.getLocation());

      return result;
   }
}
