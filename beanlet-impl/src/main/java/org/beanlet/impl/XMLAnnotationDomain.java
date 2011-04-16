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
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.beanlet.BeanletDefinitionException;
import org.beanlet.BeanletValidationException;
import org.beanlet.annotation.AbstractAnnotationDomain;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.annotation.PackageElement;
import org.beanlet.plugin.ClassResolver;
import org.beanlet.plugin.ElementAnnotationContext;
import org.beanlet.plugin.ElementAnnotationFactory;
import org.beanlet.plugin.NestedBeanletFactory;
import org.beanlet.plugin.XMLElementAnnotation;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * First, an intermediate {@code AnnotationDomain} is created from the 
 * annotations declared by the beanlet type and underlying package. 
 * This includes annotatations defined by XML and Java code. Next, classes are 
 * extracted from this {@code AnnotationDomain} by the {@code ClassResolver}. 
 * Finally, annotations are read from XML and these classes 
 * (and underlying packages). These annotations are added to the final 
 * {@code AnnotationDomain}.
 *
 * @author Leon van Zantvoort
 */
public final class XMLAnnotationDomain<T> extends AbstractAnnotationDomain {

    // PENDING: This class requires some clean up, I know!
    
    private static final ConcurrentMap<String, AtomicInteger> anonymousCounterMap;
    private static final XPath xpath;
    private static final XPathExpression BEANLET_EXPRESSION;
    private static final XPathExpression GLOBAL_PACKAGE_ANNOTATIONS_EXPRESSION;
    private static final XPathExpression GLOBAL_ANNOTATIONS_EXPRESSION;
    private static final XPathExpression MERGE_EXPRESSION;
    private static final XPathExpression ALL_EXPRESSION;
    
    static {
        // Records in this map are never cleared!
        anonymousCounterMap = new ConcurrentHashMap<String, AtomicInteger>();
        xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(BEANLET_NAMESPACE_CONTEXT);
        try {
            BEANLET_EXPRESSION = xpath.compile("/:beanlets/:beanlet");
            GLOBAL_PACKAGE_ANNOTATIONS_EXPRESSION = xpath.compile(
                    "/:beanlets/:package-annotations[not(@package)]");
            GLOBAL_ANNOTATIONS_EXPRESSION = xpath.compile(
                    "/:beanlets/:annotations[not(@type)]");
            MERGE_EXPRESSION = xpath.compile("@merge");
            ALL_EXPRESSION = xpath.compile("*");
        } catch (XPathExpressionException e) {
            throw new AssertionError(e);
        }
    }

    static String getAnonymousBeanletName(String prefix) {
        if (prefix == null) {
            prefix = Object.class.getName();
        }
        AtomicInteger counter = anonymousCounterMap.get(prefix);
        if (counter == null) {
            counter = new AtomicInteger();
            anonymousCounterMap.putIfAbsent(prefix, counter);
            counter = anonymousCounterMap.get(prefix);
        }
        assert counter != null;
        return prefix + "$" + counter.getAndIncrement();
    }
    
    /**
     * Returns a beanlet name for a top level beanlet.
     */
    private static String getBeanletName(Node beanletNode, 
            XMLAnnotationDomain parentDomain) {
        NamedNodeMap attributes = beanletNode.getAttributes();
        Node nameNode = attributes.getNamedItem("name");
        String beanletName = nameNode == null ? null : nameNode.getNodeValue();
        if (beanletName == null) {
            Node typeNode = attributes.getNamedItem("type");
            String className = typeNode == null ? null : typeNode.getNodeValue();
            if (className == null) {
                if (parentDomain != null) {
                    className = parentDomain.getBeanletType().getName();
                } else {
                    className = Object.class.getName();
                }
            }
            if (beanletName == null) {
                beanletName = getAnonymousBeanletName(className);
            }
        } else {
            if (beanletName.contains("$")) {
                throw new BeanletDefinitionException(beanletName,
                        "$ is a reserved character.");
            }
        }
        return beanletName;
    }
    
    /**
     * Returns an unmodifiable list of all beanlet annotation domains expressed
     * by the specified document.
     */
    public static List<XMLAnnotationDomain<?>> list(List<Document> documents, 
            ClassLoader loader, ElementAnnotationFactory annotationFactory, 
            ClassResolver resolver) throws BeanletDefinitionException {
        try {
            List<NestedBeanletFactoryImpl> nestedFactories = 
                    new ArrayList<NestedBeanletFactoryImpl>();
            Queue<Node> derived = 
                    new LinkedList<Node>();
            Map<String, XMLAnnotationDomain<?>> map = 
                    new HashMap<String, XMLAnnotationDomain<?>>();
            Set<String> names = new HashSet<String>();
            for (Document document : documents) {
                NodeList nodes = (NodeList) BEANLET_EXPRESSION.evaluate(document, 
                        XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node beanletNode = nodes.item(i);
                    NamedNodeMap attributes = beanletNode.getAttributes();
                    String beanletName = attributes.getNamedItem("name") == null 
                            ? null : attributes.getNamedItem("name").getNodeValue();
                    Node parentNode = attributes.getNamedItem("parent");
                    String parent = parentNode == null ? null : parentNode.getNodeValue();
                    if (parent != null) {
                        if (beanletName != null && beanletName.equals(parent)) {
                            throw new BeanletDefinitionException(beanletName, "Cyclic inheritance.");
                        }
                        derived.offer(beanletNode);
                        continue;
                    } else {
                        if (beanletName == null) {
                            beanletName = getBeanletName(beanletNode, null);
                        }
                    }
                    Map<String, XMLAnnotationDomain<?>> parentDomains = 
                            new HashMap<String, XMLAnnotationDomain<?>>(map);
                    NestedBeanletFactoryImpl nestedFactory = 
                            new NestedBeanletFactoryImpl(beanletName, loader, 
                            annotationFactory, resolver, parentDomains);
                    XMLAnnotationDomain<?> domain = createNestedDomain(beanletName, 
                            beanletNode, loader, annotationFactory, resolver, 
                            null, nestedFactory);
                    if (map.put(beanletName, domain) != null) {
                        throw new BeanletDefinitionException(beanletName,
                                "Duplicate definition of beanlet.");
                    }
                    parentDomains.put(beanletName, domain);
                    nestedFactories.add(nestedFactory);
                }
            }

            String beanletName = null;
            Node beanletNode = null;
            while ((beanletNode = derived.poll()) != null) {
                NamedNodeMap attributes = beanletNode.getAttributes();
                String parent = attributes.getNamedItem("parent").getNodeValue();
                XMLAnnotationDomain<?> parentDomain = 
                        (XMLAnnotationDomain<?>) map.get(parent);
                if (parentDomain == null) {
                    if (!names.contains(parent)) {
                        throw new BeanletDefinitionException(
                                getBeanletName(beanletNode, null), 
                                "Parent does not exist: '" + parent + "'.");
                    } else {
                        derived.add(beanletNode);
                    }
                    continue;
                }
                beanletName = getBeanletName(beanletNode, parentDomain);
                Map<String, XMLAnnotationDomain<?>> parentDomains = 
                        new HashMap<String, XMLAnnotationDomain<?>>(map);
                NestedBeanletFactoryImpl nestedFactory = 
                        new NestedBeanletFactoryImpl(beanletName, loader, 
                        annotationFactory, resolver, parentDomains);
                XMLAnnotationDomain<?> domain = createNestedDomain(beanletName,
                        beanletNode, loader, annotationFactory, resolver, 
                        parentDomain, nestedFactory);
                map.put(beanletName, domain);
                parentDomains.put(beanletName, domain);
                nestedFactories.add(nestedFactory);
            }
            List<XMLAnnotationDomain<?>> list = 
                    new ArrayList<XMLAnnotationDomain<?>>(map.values());
            for (NestedBeanletFactoryImpl nestedFactory : nestedFactories) {
                list.addAll(nestedFactory.get());
            }
            return Collections.unmodifiableList(list);
                    
        } catch (XPathExpressionException e) {
            throw new AssertionError(e);
        }
    }
    
    static <T> XMLAnnotationDomain<? extends T> createNestedDomain(
            String beanletName, Node beanletNode, ClassLoader loader, 
            ElementAnnotationFactory annotationFactory, ClassResolver resolver, 
            XMLAnnotationDomain<T> parentDomain,
            NestedBeanletFactory nestedFactory) throws 
            BeanletDefinitionException {
        NamedNodeMap attributes = beanletNode.getAttributes();
        Node typeNode = attributes.getNamedItem("type");
        Node descriptionNode = attributes.getNamedItem("description");
        Node factoryNode = attributes.getNamedItem("factory");
        Node factoryMethodNode = attributes.getNamedItem("factory-method");
        Node abstractNode = attributes.getNamedItem("abstract");
        Node parentNode = attributes.getNamedItem("parent");

        String className = typeNode == null ? null : typeNode.getNodeValue();
        String description = descriptionNode == null ? null : descriptionNode.getNodeValue();
        String factory = factoryNode == null ? null : factoryNode.getNodeValue();
        String factoryMethod = factoryMethodNode == null ? null : factoryMethodNode.getNodeValue();
        boolean abstr = abstractNode == null ? false : Boolean.valueOf(abstractNode.getNodeValue());
        String parent = parentNode == null ? null : parentNode.getNodeValue();
        final Class<? extends T> beanletType;
        if (className == null) {
            if (parentDomain != null) {
                beanletType = parentDomain.getBeanletType();
            } else {
                @SuppressWarnings("unchecked")
                Class<? extends T> tmp = (Class<? extends T>) Object.class;
                beanletType = tmp;
            }
        } else {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends T> tmp = (Class<? extends T>) 
                        loader.loadClass(className);
                beanletType = tmp;
            } catch (ClassNotFoundException e) {
                throw new BeanletDefinitionException(beanletName, 
                        "Type not found: " + className + ".");
            }
        }
        @SuppressWarnings("unchecked")
        XMLAnnotationDomain<? extends T> domain = 
                (XMLAnnotationDomain<? extends T>) 
                new XMLAnnotationDomain(
                beanletNode, beanletName, beanletType, 
                description, abstr, parent, factory, factoryMethod, 
                loader, annotationFactory, resolver, parentDomain, 
                nestedFactory);
        if (parentDomain != null) {
            domain = domain.merge(parentDomain);
        }
        return domain;
    }
    
    private final String beanletName;
    private final Class<T> beanletType;
    private final String description;
    private final boolean abstr;
    private final String parent;
    private final String factory;
    private final String factoryMethod;
    private final ClassLoader loader;
    private final NestedBeanletFactory nestedFactory;
    
    private XMLAnnotationDomain(Node beanletNode, String beanletName, 
            Class<T> beanletType, String description, boolean abstr, 
            String parent, String factory, String factoryMethod, 
            ClassLoader loader, ElementAnnotationFactory annotationFactory, 
            ClassResolver resolver, AnnotationDomain parentDomain, 
            NestedBeanletFactory nestedFactory) {
        this(getElementAnnotations(beanletNode, beanletName, beanletType, 
                description, abstr, parent, factory, factoryMethod, loader, 
                annotationFactory, resolver, parentDomain, nestedFactory), 
                beanletName, beanletType, description, abstr, parent, factory, 
                factoryMethod, loader, nestedFactory, true);
    }
    
    private XMLAnnotationDomain(List<ElementAnnotation> elementAnnotations, 
            String beanletName, Class<T> beanletType, String description, boolean abstr, 
            String parent, String factory, String factoryMethod, 
            ClassLoader loader, NestedBeanletFactory 
            nestedFactory, boolean validate) {
        super(elementAnnotations);
        this.beanletName = beanletName;
        this.beanletType = beanletType;
        this.description = description;
        this.abstr = abstr;
        this.parent = parent;
        this.factory = factory;
        this.factoryMethod = factoryMethod;
        this.loader = loader;
        this.nestedFactory = nestedFactory;
        if (validate) {
            for (ElementAnnotation ea : elementAnnotations) {
                validate(ea);
            }
        }
    }
    
    public String getBeanletName() {
        return beanletName;
    }
    
    public Class<T> getBeanletType() {
        return beanletType;
    }

    public String getDescription() {
        return description;
    }
    
    public boolean isAbstract() {
        return abstr;
    }
    
    public String getParent() {
        return parent;
    }
    
    public String getFactory() {
        return factory;
    }
    
    public String getFactoryMethod() {
        return factoryMethod;
    }

    public ClassLoader getClassLoader() {
        return loader;
    }

    private void validate(ElementAnnotation ea) {
        Target target = ea.getAnnotation().annotationType().getAnnotation(
                Target.class);
        if (target != null) {
            if (!Arrays.asList(target.value()).contains(
                    ea.getElement().getElementType())) {
                throw new BeanletValidationException(getBeanletName(), 
                        "Invalid element for annotation type. Element: " +
                        ea.getElement() + ". Annotation: " +
                        ea.getAnnotation().annotationType() + ".");
            }
        }
    }
    
    /**
     * Returns the package-annotations node that represents the specified 
     * package. If a package-annotations node exists for the specified
     * {@code packageName}, this node is selected. If not, the 
     * package-annotations node without the package attribute is selected, if
     * available. In all other cases, {@code null} is returned.
     *
     * @return package-annotations node for the specified package, or 
     * {@code null} if node does not exist.
     */
    private static Node getGlobalPackageAnnotationsNode(Node beanletNode, 
            String packageName) throws XPathExpressionException {
        Node node = (Node) xpath.evaluate(
                "/:beanlets/:package-annotations[@package='" + 
                packageName  + "']", beanletNode, XPathConstants.NODE);
        if (node == null) {
            node = (Node) GLOBAL_PACKAGE_ANNOTATIONS_EXPRESSION.evaluate(
                    beanletNode, XPathConstants.NODE);
        }
        return node;
    }

    private static Node getGlobalAnnotationsNode(Node beanletNode, 
            String typeName) throws XPathExpressionException {
        Node node = (Node) xpath.evaluate(
                "/:beanlets/:annotations[@type='" + 
                typeName + "']", beanletNode, XPathConstants.NODE);
        if (node == null) {
            node = (Node) GLOBAL_ANNOTATIONS_EXPRESSION.evaluate(
                    beanletNode, XPathConstants.NODE);
        }
        return node;
    }
    
    /**
     * Returns the annotations node that represents the specified class.
     * If a annotations node exist for the specified {@code typeName}, this node 
     * is selected. If {@code typeName} matches the {@code beanletType}, and no
     * node has been selected yet, the annotations node without a type attribute 
     * is selected. In all other cases, {@code null} is returned.
     *
     * @return package-annotations node for the specified package, or 
     * {@code null} if node does not exist.
     */
    private static Node getLocalAnnotationsNode(Node beanletNode, 
            String beanletName, Class<?> beanletType, String typeName) throws 
            XPathExpressionException {
        // PENDING: update this method's javadoc.
        Node node = (Node) xpath.evaluate(
                "./:annotations[@type='" + typeName + "']", 
                beanletNode, XPathConstants.NODE);
        if (beanletType.getName().equals(typeName)) {
            NodeList list = (NodeList) xpath.evaluate("./:annotations", 
                        beanletNode, XPathConstants.NODESET);
            final Node tmp;
            if (list.getLength() == 0) {
                tmp = beanletNode;
            } else {
                tmp = (Node) xpath.evaluate("./:annotations[not(@type)]", 
                        beanletNode, XPathConstants.NODE);
            }
            if (node == null) {
                node = tmp;
            } else if (tmp != null) {
                throw new BeanletValidationException(beanletName,
                        "Ambiguous annotation element.");
            }
        }
        return node;
    }
    
    /**
     * Returns all {@code ElementAnnotation}s from XML and the underlying class.
     */
    private static List<ElementAnnotation> getElementAnnotations(
            Node beanletNode, String beanletName, Class<?> beanletType, 
            String description, boolean abstr, String parent, String factory, 
            String factoryMethod, ClassLoader loader, 
            ElementAnnotationFactory annotationFactory, 
            ClassResolver resolver, AnnotationDomain parentDomain, 
            NestedBeanletFactory nestedFactory) {
        Set<Class> dupes = new HashSet<Class>();
        dupes.add(beanletType);

        @SuppressWarnings("unchecked")
        XMLAnnotationDomain<?> intermediate = 
                (XMLAnnotationDomain<?>)
                new XMLAnnotationDomain(getElementAnnotations(
                beanletNode, beanletName, beanletType, beanletType, description,
                abstr, parent, factory, factoryMethod, loader, 
                annotationFactory, parentDomain, nestedFactory),
                beanletName, beanletType, description, abstr, parent, factory, 
                factoryMethod, loader, nestedFactory, false);
        
        // Add annotations for other classes specified by XML.
        String className = null;
        try {
            NodeList list = (NodeList) xpath.evaluate("./:annotations/@type", 
                    beanletNode, XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); i++) {
                className = list.item(i).getNodeValue();
                Class<?> cls = loader.loadClass(className);
                if (dupes.add(cls)) {
                    intermediate = intermediate.mergeDomain(
                            getElementAnnotations(beanletNode, beanletName, 
                            beanletType, cls, description, abstr, parent, 
                            factory, factoryMethod, loader, annotationFactory, 
                            parentDomain, nestedFactory));
                }
            }
        } catch (ClassNotFoundException e) {
            throw new BeanletValidationException(beanletName, 
                    "Class not found: " + className + ".");
        } catch (XPathExpressionException e) {
            throw new AssertionError(e);
        }

        // Add annotations for resolved classes.
        for (Class<?> cls : resolver.getClasses(intermediate)) {
            if (dupes.add(cls)) {
                intermediate = intermediate.mergeDomain(
                        getElementAnnotations(beanletNode, beanletName, 
                        beanletType, cls, description, abstr, parent, factory, 
                        factoryMethod, loader, annotationFactory, parentDomain,
                        nestedFactory));
            }
        }
        return intermediate.getElements();
    }
    
    /**
     * Returns all {@code ElementAnnotation}s from XML and the underlying class.
     */
    private static List<ElementAnnotation> getElementAnnotations(
            Node beanletNode, String beanletName, Class<?> beanletType, 
            final Class<?> cls, String description, boolean abstr, String parent, 
            String factory, String factoryMethod, ClassLoader loader, 
            ElementAnnotationFactory annotationFactory, 
            AnnotationDomain parentDomain, 
            NestedBeanletFactory nestedFactory) {
        try {
            if (parentDomain == null) {
                parentDomain = AccessController.doPrivileged(new PrivilegedAction<AnnotationDomain>() {
                    public AnnotationDomain run() {
                        return AbstractAnnotationDomain.instance(cls);
                    }
                });
            }
            List<ElementAnnotation> list = new ArrayList<ElementAnnotation>();
            Package pkg = cls.getPackage();
            if (pkg != null) {
                Node node = getGlobalPackageAnnotationsNode(
                        beanletNode, pkg.getName());
                if (node != null) {
                    list.addAll(getPackageElementAnnotations(beanletName, node, 
                            pkg, loader, annotationFactory, parentDomain,
                            nestedFactory));
                }
            }
            Node localNode = getLocalAnnotationsNode(beanletNode, beanletName, 
                    beanletType, cls.getName());
            if (localNode != null) {
                list.addAll(getClassElementAnnotations(beanletName, localNode, 
                        cls, loader, annotationFactory, parentDomain,
                        nestedFactory));
            }
            @SuppressWarnings("unchecked")
            XMLAnnotationDomain<?> tmp = (XMLAnnotationDomain<?>) 
                    new XMLAnnotationDomain(
                    list, beanletName, beanletType, description, abstr, parent, 
                    factory, factoryMethod, loader, nestedFactory, false);
            
            Node globalNode = getGlobalAnnotationsNode(
                    beanletNode, cls.getName());
            if (globalNode != null) {
                tmp = tmp.mergeDomain(getClassElementAnnotations(beanletName,
                        globalNode, cls, loader, annotationFactory, 
                        parentDomain, nestedFactory));
            }
            
            AnnotationDomain domain = AccessController.doPrivileged(
                    new PrivilegedAction<AnnotationDomain>() {
                public AnnotationDomain run() {
                    return AbstractAnnotationDomain.instance(cls);
                }
            });
            return tmp.mergeList(domain.getElements());
        } catch (XPathExpressionException e) {
            assert false : e;
            return Collections.emptyList();
        }
    }
    
    private XMLAnnotationDomain<T> merge(
            XMLAnnotationDomain<? super T> domain) 
            throws BeanletDefinitionException {
        assert getParent() != null;
        assert this != domain;
        assert domain.getBeanletName().endsWith(getParent());
        
        Class<T> beanletType = getBeanletType();
        if (beanletType == null) {
            @SuppressWarnings("unchecked")
            Class<T> tmp = (Class<T>) domain.getBeanletType(); 
            beanletType = tmp;
        } else if (!domain.getBeanletType().isAssignableFrom(beanletType)) {
            // PENDING: this check might be removed in the future.
            throw new BeanletDefinitionException(getBeanletName(), 
                    "Parent's beanlet type MUST be assignable from beanlet type.");
        }
        String description = getDescription();
        if (description == null) {
            description = domain.getDescription();
        }
        String factory = getFactory();
        if (factory == null) {
            factory = domain.getFactory();
        }
        String factoryMethod = getFactoryMethod();
        if (factoryMethod == null) {
            factoryMethod = domain.getFactoryMethod();
        }
        
        // The annotation merge boolean values are not inherited!
        
        return new XMLAnnotationDomain<T>(mergeList(domain.getElements()), 
                getBeanletName(), beanletType, description, isAbstract(),
                getParent(), factory, factoryMethod, getClassLoader(), 
                nestedFactory, false);
    }
    
    /**
     * Returns a new {@code BeanletXMLAnnotationDomain} as a result of the
     * merger of {@code this} domain and the specified {@code domain}. The 
     * specified {@code list} overrides non {@code XMLElementAnnotation}s of 
     * {@code this} domain.
     */
    private XMLAnnotationDomain<T> mergeDomain(List<ElementAnnotation> elementAnnotations) {
        return new XMLAnnotationDomain<T>(mergeList(elementAnnotations), 
                getBeanletName(), getBeanletType(), getDescription(), 
                isAbstract(), getParent(), getFactory(), getFactoryMethod(), 
                getClassLoader(), nestedFactory, false);
    }
    
    /**
     * Returns a list of {@code ElementAnnotation}s as a result of the
     * merger of the element annotations of {@code this} domain and the 
     * specified {@code elementAnnotations}. The specified {@code list} 
     * overrides non {@code XMLElementAnnotation}s of {@code this} domain.
     */
    private List<ElementAnnotation> mergeList(List<ElementAnnotation> list) {
        Set<ElementAnnotation> orgList = new HashSet<ElementAnnotation>(
                getElements());
        List<ElementAnnotation> newList = new ArrayList<ElementAnnotation>();
        for (Iterator<ElementAnnotation> i = list.iterator(); i.hasNext();) {
            ElementAnnotation ea = i.next();
            ElementAnnotation org = getElementAnnotation(ea.getElement(), 
                    ea.getAnnotation().annotationType());
            if (org == null) {
                newList.add(ea);
            } else if (!(org instanceof XMLElementAnnotation)) {
                newList.add(ea);
                boolean removed = orgList.remove(org);
                assert removed;
            }
        }
        newList.addAll(orgList);
        return newList;
    }
    
    /**
     * Returns all package specific {@code ElementAnnotation} objects for the
     * specified {@code pkgNode}.
     */
    private static List<ElementAnnotation<PackageElement, Annotation>> 
            getPackageElementAnnotations(String beanletName, Node pkgNode, 
            Package pkg, ClassLoader loader, ElementAnnotationFactory factory, 
            AnnotationDomain parent, 
            NestedBeanletFactory nestedFactory) {
        List<ElementAnnotation<PackageElement, Annotation>> eas = 
                new ArrayList<ElementAnnotation<PackageElement, Annotation>>();
        try {
            NodeList nodeList = (NodeList) ALL_EXPRESSION.evaluate(pkgNode, 
                    XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node annotationNode = nodeList.item(i);
                Node mergeNode = (Node) MERGE_EXPRESSION.evaluate(
                        annotationNode, XPathConstants.NODE);
                boolean merge = mergeNode == null ? false : Boolean.valueOf(
                        mergeNode.getNodeValue());

                ElementAnnotationContext ctx = 
                        new ElementAnnotationContextImpl(
                        beanletName, loader, merge ? parent : null, 
                        nestedFactory);
                @SuppressWarnings("unchecked")
                ElementAnnotation<PackageElement, Annotation> e = factory.
                        getElementAnnotation(annotationNode, pkg, ctx);
                if (e != null) {
                    eas.add(e);
                }
            }
        } catch (XPathExpressionException e) {
            assert false : e;
        }
        return eas;
    }

    /**
     * Returns all class specific {@code ElementAnnotation} objects for the 
     * specified {@code node}.
     *
     * @param node
     * @param cls
     */
    private static List<ElementAnnotation> getClassElementAnnotations(
            String beanletName, Node node, Class<?> cls, ClassLoader loader, 
            ElementAnnotationFactory factory, AnnotationDomain parent,
            NestedBeanletFactory nestedFactory) {
        List<ElementAnnotation> eas = new ArrayList<ElementAnnotation>();
        try {
            NodeList nodeList = (NodeList) ALL_EXPRESSION.evaluate(node, 
                    XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node annotationNode = nodeList.item(i);
                Node mergeNode = (Node) MERGE_EXPRESSION.evaluate(
                        annotationNode, XPathConstants.NODE);
                boolean merge = mergeNode == null ? false : Boolean.valueOf(
                        mergeNode.getNodeValue());

                ElementAnnotationContext ctx = 
                        new ElementAnnotationContextImpl(
                        beanletName, loader, merge ? parent : null, 
                        nestedFactory);
                @SuppressWarnings("unchecked")
                ElementAnnotation e = factory.getElementAnnotation(
                        annotationNode, cls, ctx);
                if (e != null) {
                    eas.add(e);
                }
            }
        } catch (XPathExpressionException e) {
            assert false : e;
        }
        return eas;
    }
}
