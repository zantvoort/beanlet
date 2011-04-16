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
package org.beanlet.impl;

import static org.beanlet.common.BeanletConstants.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.beanlet.BeanletApplicationException;
import org.beanlet.common.AbstractProvider;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.BeanletConfigurationValidator;
import org.beanlet.common.BeanletConfigurationValidators;
import org.beanlet.plugin.ClassResolver;
import org.beanlet.common.ClassResolvers;
import org.beanlet.common.ElementAnnotationFactories;
import org.beanlet.plugin.ElementAnnotationFactory;
import org.beanlet.plugin.spi.BeanletConfigurationValidatorProvider;
import org.beanlet.plugin.spi.ClassResolverProvider;
import org.beanlet.plugin.spi.ElementAnnotationFactoryProvider;
import org.jargo.ComponentAlias;
import org.jargo.ComponentConfiguration;
import org.jargo.ComponentUnit;
import org.jargo.deploy.Deployable;
import org.jargo.deploy.Deployer;
import org.jargo.spi.ComponentAliasProvider;
import org.jargo.spi.ComponentConfigurationProvider;
import org.jargo.spi.Provider;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Leon van Zantvoort
 */
public final class BeanletConfigurationProviderImpl extends AbstractProvider
        implements ComponentConfigurationProvider, ComponentAliasProvider, Deployer {
    
    private static final Method	FIND_RESOURCES;
    
    private static final XPath xpath;
    private static final XPathExpression IMPORT_RESOURCE_EXPRESSION;
    private static final XPathExpression IMPORT_OPTIONAL_EXPRESSION;
    private static final XPathExpression ALIAS_EXPRESSION;
    
    static {
        try {
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
            xpath.setNamespaceContext(BEANLET_NAMESPACE_CONTEXT);
            try {
                IMPORT_RESOURCE_EXPRESSION = xpath.compile(
                        "/:beanlets/:import/@resource");
                IMPORT_OPTIONAL_EXPRESSION = xpath.compile("../@optional");
                ALIAS_EXPRESSION = xpath.compile("/:beanlets/:alias");
            } catch (XPathExpressionException e) {
                throw new AssertionError(e);
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
    
    private final Logger logger;
    private final List<ElementAnnotationFactoryProvider> annotationFactoryProviders;
    private final AtomicReference<ElementAnnotationFactory> annotationFactory;
    private final List<ClassResolverProvider> resolverProviders;
    private final AtomicReference<ClassResolver> resolver;
    private final List<BeanletConfigurationValidatorProvider> validatorProviders;
    private final AtomicReference<BeanletConfigurationValidator> validator;
    private final WeakHashMap<ClassLoader, List<Document>> documentMap;
    
    public BeanletConfigurationProviderImpl() {
        logger = Logger.getLogger(getClass().getName());
        annotationFactoryProviders = 
                new CopyOnWriteArrayList<ElementAnnotationFactoryProvider>();
        annotationFactory = new AtomicReference<ElementAnnotationFactory>(
                new ElementAnnotationFactories(null));
        resolverProviders = new CopyOnWriteArrayList<ClassResolverProvider>();
        resolver = new AtomicReference<ClassResolver>(new ClassResolvers(null));
        validatorProviders = 
                new CopyOnWriteArrayList<BeanletConfigurationValidatorProvider>();
        validator = new AtomicReference<BeanletConfigurationValidator>(
                new BeanletConfigurationValidators(null));

        // The WeakHashMap's key is not reference by the map's value.
        documentMap = new WeakHashMap<ClassLoader, List<Document>>();
    }
    
    public List<ComponentAlias> getComponentAliases(ComponentUnit unit) {
        List<ComponentAlias> aliases = new ArrayList<ComponentAlias>();
        try {
            List<Document> documents = getDocuments(unit.getClassLoader());
            for (Document document : documents) {
                NodeList nodeList = (NodeList) ALIAS_EXPRESSION.evaluate(document,
                        XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    NamedNodeMap attributes = node.getAttributes();
                    Node nameNode = attributes.getNamedItem("name");
                    Node aliasNode = attributes.getNamedItem("alias");
                    Node overrideNode = attributes.getNamedItem("override");
                    if (nameNode == null) {
                        throw new XPathExpressionException("name attribute");
                    }
                    if (aliasNode == null) {
                        throw new XPathExpressionException("alias attribute");
                    }
                    final String name = nameNode.getNodeValue();
                    final String alias = aliasNode.getNodeValue();
                    final boolean override = overrideNode == null ?
                        false : Boolean.valueOf(overrideNode.getNodeValue());
                    aliases.add(new ComponentAlias() {
                        public String getComponentName() {
                            return name;
                        }
                        public String getComponentAlias() {
                            return alias;
                        }
                        public boolean override() {
                            return override;
                        }
                    });
                }
            }
        } catch (XPathExpressionException e) {
            assert false : e;
            throw new BeanletApplicationException(e);
        }
        return Collections.unmodifiableList(aliases);
    }
    
    public List<ComponentConfiguration<?>> getComponentConfigurations(
            ComponentUnit unit) {
        List<ComponentConfiguration<?>> configurations =
                new ArrayList<ComponentConfiguration<?>>();
        
        List<XMLAnnotationDomain<?>> domains =
                XMLAnnotationDomain.list(getDocuments(unit.getClassLoader()),
                unit.getClassLoader(), annotationFactory.get(), resolver.get());
        for (XMLAnnotationDomain<?> tmp : domains) {
            @SuppressWarnings("unchecked")
            XMLAnnotationDomain<Object> domain = 
                    (XMLAnnotationDomain<Object>) tmp;
            if (!domain.isAbstract()) {
                BeanletConfiguration<?> configuration =
                        new BeanletConfigurationImpl<Object>(unit, domain);
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest(String.valueOf(configuration));
                }
                validator.get().validate(configuration);
                configurations.add(configuration);
            }
        }
        return Collections.unmodifiableList(configurations);
    }
    
    public void setParent(Deployer deployer) {
        // Do nothing.
    }
    
    public void deploy(Deployable deployable) throws Exception {
        if (deployable instanceof ElementAnnotationFactoryProvider) {
            annotationFactoryProviders.add((ElementAnnotationFactoryProvider) 
                    deployable);
            final List<ElementAnnotationFactory> tmp =
                    new ArrayList<ElementAnnotationFactory>();
            for (ElementAnnotationFactoryProvider provider : 
                    getSortedList(annotationFactoryProviders)) {
                tmp.addAll(provider.getElementAnnotationFactories());
            }
            annotationFactory.set(new ElementAnnotationFactories(tmp));
        }
        if (deployable instanceof ClassResolverProvider) {
            resolverProviders.add((ClassResolverProvider) deployable);
            final List<ClassResolver> tmp = new ArrayList<ClassResolver>();
            for (ClassResolverProvider provider : getSortedList(resolverProviders)) {
                tmp.addAll(provider.getClassResolvers());
            }
            resolver.set(new ClassResolvers(tmp));
        }
        if (deployable instanceof BeanletConfigurationValidatorProvider) {
            validatorProviders.add((BeanletConfigurationValidatorProvider) deployable);
            final List<BeanletConfigurationValidator> tmp =
                    new ArrayList<BeanletConfigurationValidator>();
            for (BeanletConfigurationValidatorProvider provider :
                getSortedList(validatorProviders)) {
                    tmp.addAll(provider.getBeanletConfigurationValidators());
                }
                validator.set(new BeanletConfigurationValidators(tmp));
        }
    }
    
    public void undeploy(Deployable deployable) throws Exception {
        if (deployable instanceof ElementAnnotationFactoryProvider) {
            annotationFactoryProviders.remove((ElementAnnotationFactoryProvider) 
                    deployable);
            List<ElementAnnotationFactory> tmp =
                    new ArrayList<ElementAnnotationFactory>();
            for (ElementAnnotationFactoryProvider provider : getSortedList(
                    annotationFactoryProviders)) {
                tmp.addAll(provider.getElementAnnotationFactories());
            }
            annotationFactory.set(new ElementAnnotationFactories(tmp));
        }
        if (deployable instanceof ClassResolverProvider) {
            resolverProviders.remove((ClassResolverProvider) deployable);
            List<ClassResolver> tmp = new ArrayList<ClassResolver>();
            for (ClassResolverProvider provider : getSortedList(
                    resolverProviders)) {
                tmp.addAll(provider.getClassResolvers());
            }
            resolver.set(new ClassResolvers(tmp));
        }
        if (deployable instanceof BeanletConfigurationValidatorProvider) {
            validatorProviders.remove((BeanletConfigurationValidatorProvider) 
                    deployable);
            List<BeanletConfigurationValidator> tmp =
                    new ArrayList<BeanletConfigurationValidator>();
            for (BeanletConfigurationValidatorProvider provider :
                getSortedList(validatorProviders)) {
                    tmp.addAll(provider.getBeanletConfigurationValidators());
                }
                validator.set(new BeanletConfigurationValidators(tmp));
        }
    }
    
    /**
     * Sorts the specified {@code providers} list according to the rules of
     * the {@code SequentialDeployable} interface.
     */
    private <T extends Provider> List<T> getSortedList(
            List<T> providers) {
        List<T> newList =
                new ArrayList<T>(providers);
        Collections.sort(newList,
                new SequentialDeployableComparator<T>());
        return newList;
    }
    
    /**
     * Returns all beanlet documents that are available through the specified
     * class {@code loader}.
     */
    private synchronized List<Document> getDocuments(ClassLoader loader) {
        List<Document> list = documentMap.get(loader);
        if (list == null) {
            list = getDocuments(BEANLET_XML, loader);
            documentMap.put(loader, list);
        }
        return list;
    }
    
    private List<Document> getDocuments(String resource, ClassLoader loader) {
        List<Document> documents = new ArrayList<Document>();
        if (loader != null) {
            try {
                List<URL> urls = new ArrayList<URL>();
                @SuppressWarnings("unchecked")
                Enumeration<URL> e1 = (Enumeration<URL>) FIND_RESOURCES.
                        invoke(loader, resource);
                while (e1.hasMoreElements()) {
                    urls.add(e1.nextElement());
                }
                @SuppressWarnings("unchecked")
                Enumeration<URL> e2 = (Enumeration<URL>) FIND_RESOURCES.
                        invoke(loader, "META-INF/" + resource);
                while (e2.hasMoreElements()) {
                    urls.add(e2.nextElement());
                }
                Set<URL> dupes = new HashSet<URL>();
                for (URL url : urls) {
                    documents.addAll(getDocuments(url, loader, dupes));
                }
            } catch (IllegalAccessException e) {
                assert false : e;
            } catch (InvocationTargetException e) {
                throw new BeanletApplicationException("Failed to load resource: " + 
                        resource, e.getTargetException());
            } catch (FileNotFoundException e) {
                throw new BeanletApplicationException("Failed to load resource: " + 
                        resource + ".", e);
            }
        }
        return documents;
    }

    private List<Document> getDocuments(URL url, final ClassLoader loader,
            Set<URL> dupes) throws FileNotFoundException {
        List<Document> documents = new ArrayList<Document>();
        try {
            if (dupes.add(url)) {
                InputStream stream = null;
                try {
                    logger.finest("Loading beanlets from " + 
                            url.toExternalForm() + ".");
                    DocumentBuilderFactory factory = getDocumentBuilderFactory();
                    try {
                        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                                XMLConstants.W3C_XML_SCHEMA_NS_URI);
                    } catch (IllegalArgumentException e) {
                        // PENDING: handle exception
                        throw e;
                    }
                    
                    // Checks whether the document is well formed.
                    DocumentBuilder checkingBuilder = factory.newDocumentBuilder();
                    URLConnection c = url.openConnection();
                    c.setUseCaches(false);
                    InputStream s = new BufferedInputStream(c.getInputStream());
                    try {
                        checkingBuilder.parse(s, url.toExternalForm());
                    } finally {
                        s.close();
                    }
                    
                    factory.setNamespaceAware(true);
                    factory.setValidating(true);
                    // Validates the document according to the schema definition.
                    DocumentBuilder validatingBuilder = factory.newDocumentBuilder();
                    validatingBuilder.setEntityResolver(new EntityResolver() {
                        public InputSource resolveEntity(String publicId, 
                                String systemId) throws SAXException,IOException {
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
                                            return loader.getResourceAsStream(p);
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
                    });
                    validatingBuilder.setErrorHandler(new ErrorHandler() {
                        public void error(SAXParseException e) throws SAXException {
                            throw e;
                        }
                        public void fatalError(SAXParseException e) throws SAXException {
                            throw e;
                        }
                        public void warning(SAXParseException e) throws SAXException {
                            logger.warning(e.getMessage());
                        }
                    });
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(false);
                    stream = rewriteStream(connection.getInputStream(), getProperties(loader));
                    Document document = validatingBuilder.parse(stream,
                            url.toExternalForm());
                    documents.add(document);
                    NodeList nodes = (NodeList) IMPORT_RESOURCE_EXPRESSION.
                            evaluate(document, XPathConstants.NODESET);
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node optionalNode = (Node) IMPORT_OPTIONAL_EXPRESSION.evaluate(
                                nodes.item(i), XPathConstants.NODE);
                        String name = nodes.item(i).getNodeValue();
                        try {
                            final URL newUrl;
                            URI nameUri = new URI(name);
                            if (nameUri.isAbsolute()) {
                                newUrl = nameUri.toURL();
                            } else {
                                URI uri = url.toURI();
                                URI tmp = new URI(uri.getRawSchemeSpecificPart());
                                if (tmp.isAbsolute()) {
                                    newUrl = new URL(uri.getScheme() + ":" + 
                                            tmp.resolve(name).toASCIIString());
                                } else {
                                    newUrl = new URL(uri.resolve(name).
                                            toASCIIString());
                                }
                            }
                            documents.addAll(getDocuments(newUrl, loader, dupes));
                        } catch (FileNotFoundException e) {
                            if (optionalNode == null || !Boolean.valueOf(
                                    optionalNode.getNodeValue())) {
                                throw new BeanletApplicationException(
                                        "Import not found. Document: " + url + 
                                        ". Import: " + name + ".", e);
                            }
                        } catch (MalformedURLException e) {
                            throw new BeanletApplicationException(
                                    "Import not valid. Document: " + url + 
                                    ". Import: " + name + ".", e);

                        } catch (URISyntaxException e) {
                            throw new BeanletApplicationException(
                                    "Import not valid. Document: " + url + 
                                    ". Import: " + name + ".", e);
                        }
                    }
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw e;
        } catch (BeanletApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanletApplicationException("Failed to load document: " + 
                    url + ".", e);
        }
        return documents;
    }
    
    private Properties getProperties(ClassLoader loader) {
        try {
            final Properties properties = new Properties();
            List<URL> urls = new ArrayList<URL>();
            @SuppressWarnings("unchecked")
            Enumeration<URL> e2 = (Enumeration<URL>) FIND_RESOURCES.
                    invoke(loader, "META-INF/" + BEANLET_PROPERTIES);
            while (e2.hasMoreElements()) {
                urls.add(e2.nextElement());
            }
            for (ListIterator<URL> i = urls.listIterator(urls.size()); 
                    i.hasPrevious();) {
                URLConnection c = i.previous().openConnection();
                c.setUseCaches(false);
                InputStream s = c.getInputStream();
                if (s != null) {
                    try {
                        s = new BufferedInputStream(s);
                        properties.load(s);
                    } finally {
                        s.close();
                    }
                }
            }
            try {
                AccessController.doPrivileged(
                        new PrivilegedExceptionAction<Object>() {
                    public Object run() throws Exception {
                        File f = new File(BEANLET_PROPERTIES);
                        // PERMISSION: java.io.FilePermission beanlet.properties read
                        if (f.canRead()) {
                            InputStream s = new BufferedInputStream(
                                    new FileInputStream(BEANLET_PROPERTIES));
                            try {
                                properties.load(s);
                            } finally {
                                s.close();
                            }
                        }
                        return null;
                    }
                });
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    // PERMISSION: java.util.PropertyPermission * read,write
                    properties.putAll(System.getProperties());
                    return null;
                }
            });
            return properties;
        } catch (Exception e) {
            throw new BeanletApplicationException("Failed to read beanlet " +
                    "properties.", e);
        }
    }

    private InputStream rewriteStream(InputStream stream, 
            Properties properties) throws IOException {
        try {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream));

            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(replace(line, properties) + "\n");
            }

            return new ByteArrayInputStream(builder.toString().getBytes());
        } finally {
            stream.close();
        }
    }    

    private String replace(String line, Properties properties) throws 
            BeanletApplicationException {
        String modified = line;
        int x = 0;
        while ((x = modified.indexOf("${", x)) != -1) {
            if (x > 0 && modified.charAt(x - 1) == '\\') {
                x++;
                continue;
            }
            int y = x + 2;
            modified = modified.substring(0, y) + replace(modified.substring(y), 
                    properties);
            while ((y = modified.indexOf("}", y)) != -1) {
                if (modified.charAt(y - 1) == '\\') {
                    y++;
                    continue;
                }
                final String placeholder = modified.substring(x + 2, y);
                final String key;
                final String defaultValue;
                final String value;
                String[] s = placeholder.split("=", 2);
                key = s[0];
                if (s.length == 2) {
                    defaultValue = s[1];
                } else {
                    defaultValue = null;
                }

                String tmp = properties.getProperty(key);
                if (defaultValue == null) {
                    if (tmp == null) {
                        throw new BeanletApplicationException(
                                "Beanlet property not set: '" + key + "'. " +
                                "Specify property in beanlet.properties, " +
                                "or system properties.");
                    } else {
                        value = tmp;
                    }
                } else {
                    if (tmp == null) {
                        value = defaultValue;
                    } else {
                        value = tmp;
                    }
                }
                modified = modified.replace("${" + placeholder + "}", value);
                x = y;
                break;
            }
            x++;
        }
        return modified;
    }
    
    /**
     * Returns the default JDK DocumentBuilderFactory.
     */
    private static DocumentBuilderFactory getDocumentBuilderFactory() {
        try {
            return (DocumentBuilderFactory) Class.forName(
                    "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl").
                    newInstance();
        } catch (Exception e) {
            // Fallback.
            return DocumentBuilderFactory.newInstance();
        }
    }
}
