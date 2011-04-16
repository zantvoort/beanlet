/*
 * ============================================================================
 * GNU Lesser General Public License
 * ============================================================================
 *
 * Beanlet - JSE Application Container.
 * Copyright (C) 2006  Leon van Zantvoort
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Leon van Zantvoort
 * 243 Acalanes Drive #11
 * Sunnyvale, CA 94086
 * USA
 *
 * zantvoort@users.sourceforge.net
 * http://beanlet.org
 */
package org.beanlet.persistence.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import javax.persistence.PersistenceException;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.jargo.ComponentUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Leon van Zantvoort
 */
public final class BeanletPersistenceUnitInfoFactory {
    
    private static final Logger logger;
    private static final Method	FIND_RESOURCES;
    private static final XPath xpath;
    
    private static final XPathExpression TRANSACTION_TYPE;
    private static final XPathExpression EXCLUDE_UNLISTED_CLASSES;
    private static final XPathExpression JAR_FILE_URLS;
    private static final XPathExpression JTA_DATA_SOURCE;
    private static final XPathExpression MANAGED_CLASS_NAMES;
    private static final XPathExpression MAPPING_FILE_NAMES;
    private static final XPathExpression NON_JTA_DATA_SOURCE;
    private static final XPathExpression PERSISTENCE_PROVIDER_CLASSNAME;
    private static final XPathExpression PERSISTENCE_UNIT_NAME;
    private static final XPathExpression PROPERTIES;
    private static final XPathExpression VERSION;
    private static final XPathExpression SHARED_CACHE_MODE;
    private static final XPathExpression VALIDATION_MODE;

    private static final Map<ClassLoader, BeanletPersistenceUnitInfoFactory> factories;
    
    static {
        try {
            logger = Logger.getLogger(
                    BeanletPersistenceUnitInfoFactory.class.getName());
            try {
                FIND_RESOURCES = AccessController.doPrivileged(
                        new PrivilegedExceptionAction<Method>() {
                    public Method run() throws Exception {
                        // PENDING: alternative: subclassing ClassLoader?
                        
                        // PERMISSION: java.lang.RuntimePermission accessDeclaredMembers
                        Method method = ClassLoader.class.
                                getDeclaredMethod("findResources", String.class);
                        
                        // PERMISSION: java.lang.reflect.ReflectPermission suppressAccessChecks
                        method.setAccessible(true);
                        return method;
                    }
                });
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
            xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new SimpleNamespaceContext());
            try {
                TRANSACTION_TYPE = xpath.compile("@transaction-type");
                EXCLUDE_UNLISTED_CLASSES = xpath.compile("./:exclude-unlisted-classes/text()");
                JAR_FILE_URLS = xpath.compile("./:jar-file/text()");
                JTA_DATA_SOURCE = xpath.compile("./:jta-data-source/text()");
                MANAGED_CLASS_NAMES = xpath.compile("./:class/text()");
                MAPPING_FILE_NAMES = xpath.compile("./:mapping-file/text()");
                NON_JTA_DATA_SOURCE = xpath.compile("./:non-jta-data-source/text()");
                PERSISTENCE_PROVIDER_CLASSNAME = xpath.compile("./:provider/text()");
                PERSISTENCE_UNIT_NAME = xpath.compile("@name");
                PROPERTIES = xpath.compile("./:properties/:property");
                VERSION = xpath.compile("@version");
                SHARED_CACHE_MODE = xpath.compile("./:shared-cache-mode/text()");
                VALIDATION_MODE = xpath.compile("./:validation-mode/text()");
            } catch (XPathExpressionException e) {
                throw new AssertionError(e);
            }
            factories = new HashMap<ClassLoader,
                    BeanletPersistenceUnitInfoFactory>();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
    
    public static synchronized BeanletPersistenceUnitInfoFactory getInstance(
            final ComponentUnit componentUnit) {
        BeanletPersistenceUnitInfoFactory factory = factories.get(
                componentUnit.getClassLoader());
        if (factory == null) {
            factory = new BeanletPersistenceUnitInfoFactory(componentUnit);
            factories.put(componentUnit.getClassLoader(), factory);
            componentUnit.addDestroyHook(new Runnable() {
                public void run() {
                    synchronized (BeanletPersistenceUnitInfoFactory.class) {
                        factories.remove(componentUnit.getClassLoader());
                    }
                }
            });
        }
        return factory;
    }
    
    private ComponentUnit componentUnit;
    private ConcurrentMap<String, BeanletPersistenceUnitInfo> cache;
    
    private BeanletPersistenceUnitInfoFactory(ComponentUnit componentUnit) {
        this.componentUnit = componentUnit;
        this.cache = new ConcurrentHashMap<String, BeanletPersistenceUnitInfo>();
    }
    
    public BeanletPersistenceUnitInfo getPersistenceUnitInfo(
            String persistenceUnitName) throws PersistenceException {
        BeanletPersistenceUnitInfo unitInfo = cache.get(persistenceUnitName);
        if (unitInfo == null) {
            try {
                @SuppressWarnings("unchecked")
                Enumeration<URL> urls = (Enumeration<URL>) FIND_RESOURCES.
                        invoke(componentUnit.getClassLoader(), 
                        "META-INF/persistence.xml");
                
                DocumentBuilderFactory factory = DocumentBuilderFactory.
                        newInstance();
                factory.setAttribute(
                        "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                        XMLConstants.W3C_XML_SCHEMA_NS_URI);
                factory.setNamespaceAware(true);
                factory.setValidating(true);
                
                // Validates the document according to the schema definition.
                DocumentBuilder builder = factory.newDocumentBuilder();
                builder.setEntityResolver(new SimpleEntityResolver());
                builder.setErrorHandler(new SimpleErrorHandler());
                
                List<Node> nodes = new ArrayList<Node>();
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    logger.finest("Looking up persistence unit info '" +
                            persistenceUnitName + "' at " + url.toExternalForm() + ".");
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(false);
                    InputStream stream = new BufferedInputStream(connection.getInputStream());
                    try {
                        Document document = builder.parse(stream, url.toExternalForm());
                        final Node node;
                        if (persistenceUnitName == null || persistenceUnitName.equals("")) {
                            NodeList tmp = (NodeList) xpath.evaluate(
                                    "/:persistence/:persistence-unit", document,
                                    XPathConstants.NODESET);
                            if (tmp.getLength() == 0) {
                                continue;
                            } else if (tmp.getLength() > 1) {
                                throw new PersistenceException("Multiple persistence units found.");
                            } else {
                                node = tmp.item(0);
                            }
                        } else {
                            node = (Node) xpath.evaluate(
                                    "/:persistence/:persistence-unit[@name='" +
                                    persistenceUnitName + "']", document,
                                    XPathConstants.NODE);
                        }
                        if (node != null) {
                            // Alternative for ComponentUnit.getURL().
//                            if (connection instanceof JarURLConnection) {
//                                rootUrl = ((JarURLConnection) connection).
//                                        getJarFileURL();
//                            } else {
//                                rootUrl = url.toURI().resolve("..").toURL();
//                            }
                            logger.finest("Found persistence unit info '" +
                                    persistenceUnitName + "' at " + 
                                    componentUnit.getURL() + ".");
                            nodes.add(node);
                            break;
                        }
                    } finally {
                        stream.close();
                    }
                }
                if (nodes.isEmpty()) {
                    if (persistenceUnitName == null || persistenceUnitName.equals("")) {
                        throw new PersistenceException("No persistence unit found.");
                    } else {
                        throw new PersistenceException("No persistence unit " +
                                "found for '" + persistenceUnitName + "'.");
                    }
                } else if (nodes.size() > 1) {
                    if (persistenceUnitName == null || persistenceUnitName.equals("")) {
                        throw new PersistenceException("Multiple persistence units found.");
                    } else {
                        throw new PersistenceException("Multiple persistence units " +
                                "found for '" + persistenceUnitName + "'.");
                    }
                }
                unitInfo = buildPersistenceUnitInfo(nodes.get(0));
                cache.putIfAbsent(persistenceUnitName, unitInfo);
            } catch (PersistenceException e) {
                throw e;
            } catch (InvocationTargetException e) {
                throw new PersistenceException(e.getTargetException());
            } catch (Exception e) {
                throw new PersistenceException(e);
            }
        }
        unitInfo = cache.get(persistenceUnitName);
        assert unitInfo != null;
        return unitInfo;
    }
    
    
    private BeanletPersistenceUnitInfo buildPersistenceUnitInfo(
            Node node) throws PersistenceException {
        final PersistenceUnitTransactionType transactionType;
        final boolean excludeUnlistedClasses;
        final ClassLoader classLoader = componentUnit.getClassLoader();
        final List<URL> jarFileUrls;
        final String jtaDataSource;
        final List<String> managedClassNames;
        final List<String> mappingFileNames;
        final ClassLoader newTempClassLoader = new ClassLoader(componentUnit.getClassLoader()){};
        final String nonJtaDataSource;
        final String persistenceProviderClassName;
        final String persistenceUnitName;
        final URL persistenceUnitRootUrl = componentUnit.getURL();
        final Properties properties;
        final String persistenceXMLSchemaVersion;
        final SharedCacheMode sharedCacheMode;
        final ValidationMode validationMode;

        try {
            Node transactionTypeNode = (Node) TRANSACTION_TYPE.
                    evaluate(node, XPathConstants.NODE);
            if (transactionTypeNode == null) {
                transactionType = PersistenceUnitTransactionType.JTA;
            } else {
                transactionType = PersistenceUnitTransactionType.valueOf(
                        transactionTypeNode.getNodeValue());
            }
            
            Node excludeUnlistedClassesNode = (Node) EXCLUDE_UNLISTED_CLASSES.
                    evaluate(node, XPathConstants.NODE);
            if (excludeUnlistedClassesNode == null) {
                excludeUnlistedClasses = false;
            } else {
                excludeUnlistedClasses = Boolean.valueOf(
                        excludeUnlistedClassesNode.getNodeValue());
            }
            
            List<URL> tmpJarFileUrls = new ArrayList<URL>();
            NodeList jarFileUrlsList = (NodeList) JAR_FILE_URLS.
                    evaluate(node, XPathConstants.NODESET);
            for (int i = 0; i < jarFileUrlsList.getLength(); i++) {
                Node tmp = jarFileUrlsList.item(i);
                tmpJarFileUrls.add(new URL(tmp.getNodeValue()));
            }
            jarFileUrls = Collections.unmodifiableList(tmpJarFileUrls);
            
            String tmpJtaDataSource = (String) JTA_DATA_SOURCE.evaluate(node,
                    XPathConstants.STRING);
            jtaDataSource = tmpJtaDataSource.equals("") ? null : tmpJtaDataSource;
            
            List<String> tmpManagedClassNames = new ArrayList<String>();
            NodeList managedClassNamesList = (NodeList) MANAGED_CLASS_NAMES.
                    evaluate(node, XPathConstants.NODESET);
            for (int i = 0; i < managedClassNamesList.getLength(); i++) {
                Node tmp = managedClassNamesList.item(i);
                tmpManagedClassNames.add(tmp.getNodeValue());
            }
            managedClassNames = Collections.unmodifiableList(tmpManagedClassNames);
            
            List<String> tmpMappingFileNames = new ArrayList<String>();
            NodeList mappingFileNamesList = (NodeList) MAPPING_FILE_NAMES.
                    evaluate(node, XPathConstants.NODESET);
            for (int i = 0; i < mappingFileNamesList.getLength(); i++) {
                Node tmp = mappingFileNamesList.item(i);
                tmpMappingFileNames.add(tmp.getNodeValue());
            }
            mappingFileNames = Collections.unmodifiableList(tmpMappingFileNames);
            
            String tmpNonJtaDataSource = (String) NON_JTA_DATA_SOURCE.
                    evaluate(node, XPathConstants.STRING);
            nonJtaDataSource = tmpNonJtaDataSource.equals("") ? null : tmpNonJtaDataSource;
            
            String tmpPersistenceProviderClassName = (String) PERSISTENCE_PROVIDER_CLASSNAME.
                    evaluate(node, XPathConstants.STRING);
            persistenceProviderClassName = 
                    tmpPersistenceProviderClassName.equals("") ? null : 
                    tmpPersistenceProviderClassName;
            
            persistenceUnitName = (String) PERSISTENCE_UNIT_NAME.evaluate(node,
                    XPathConstants.STRING);
            
            properties = new Properties();
            NodeList propertiesList = (NodeList) PROPERTIES.evaluate(node,
                    XPathConstants.NODESET);
            for (int i = 0; i < propertiesList.getLength(); i++) {
                Node tmp = propertiesList.item(i);
                String name = (String) xpath.evaluate("@name", tmp, XPathConstants.STRING);
                String value = (String) xpath.evaluate("@value", tmp, XPathConstants.STRING);
                properties.setProperty(name, value);
            }

            Node versionNode = (Node) VERSION.
                    evaluate(node, XPathConstants.NODE);
            if (versionNode == null) {
                persistenceXMLSchemaVersion = "2.0";
            } else {
                persistenceXMLSchemaVersion = versionNode.getNodeValue();
            }

            Node sharedCacheModeNode = (Node) SHARED_CACHE_MODE.
                    evaluate(node, XPathConstants.NODE);
            if (sharedCacheModeNode == null) {
                sharedCacheMode = SharedCacheMode.ENABLE_SELECTIVE;
            } else {
                sharedCacheMode = SharedCacheMode.valueOf(sharedCacheModeNode.getNodeValue());
            }

            Node validationModeNode = (Node) VALIDATION_MODE.
                    evaluate(node, XPathConstants.NODE);
            if (validationModeNode == null) {
                validationMode = ValidationMode.AUTO;
            } else {
                validationMode = ValidationMode.valueOf(validationModeNode.getNodeValue());
            }

        } catch (PersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
        
        // PENDING: introduce these checks?
//        if (transactionType == PersistenceUnitTransactionType.JTA) {
//            if (jtaDataSource == null) {
//                throw new PersistenceException("JTA persistence unit '" + 
//                        persistenceUnitName + "' does not specify JTA data " +
//                        "source.");
//            }
//        } else {
//            if (nonJtaDataSource == null) {
//                throw new PersistenceException("RESOURCE_LOCAL persistence unit '" + 
//                        persistenceUnitName + "' does not specify non JTA data " +
//                        "source.");
//            }
//        }
        
        return new BeanletPersistenceUnitInfoImpl(
                transactionType,
                excludeUnlistedClasses,
                classLoader,
                jarFileUrls,
                jtaDataSource,
                managedClassNames,
                mappingFileNames,
                newTempClassLoader,
                nonJtaDataSource,
                persistenceProviderClassName,
                persistenceUnitName,
                persistenceUnitRootUrl,
                properties,
                persistenceXMLSchemaVersion,
                sharedCacheMode,
                validationMode
        );
    }
    
    private class SimpleEntityResolver implements EntityResolver {
        
        public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException, IOException {
            try {
                final InputSource source;
                URI uri = new URI(systemId);
                String path = uri.getPath();
                if (path != null) {
                    if (path.startsWith("/")) {
                        path = path.substring(1);
                    }
                    final String p = path;
                    InputStream stream = AccessController.
                            doPrivileged(new PrivilegedAction<InputStream>() {
                        public InputStream run() {
                            return componentUnit.getClassLoader().
                                    getResourceAsStream(p);
                        }
                    });
                    if (stream != null) {
                        source = new InputSource(stream);
                    } else {
                        source = new InputSource(systemId);
                    }
                } else {
                    source = new InputSource(systemId);
                }
                source.setPublicId(publicId);
                return source;
            } catch (URISyntaxException e) {
                throw new SAXException(e);
            }
        }
    }
    
    private class SimpleErrorHandler implements ErrorHandler {
        
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }
        
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
        
        public void warning(SAXParseException e) throws SAXException {
            logger.warning(e.getMessage());
        }
    }
    
    private static class SimpleNamespaceContext implements NamespaceContext {
        
        public String getNamespaceURI(String prefix) {
            return "http://java.sun.com/xml/ns/persistence";
        }
        
        public String getPrefix(String namespaceURI) {
            return XMLConstants.DEFAULT_NS_PREFIX;
        }
        
        public Iterator getPrefixes(String namespaceURI) {
            return Collections.singleton(
                    XMLConstants.DEFAULT_NS_PREFIX).iterator();
        }
    };
    
}