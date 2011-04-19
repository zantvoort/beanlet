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
package org.beanlet.common;

import org.beanlet.plugin.ElementAnnotationContext;
import org.beanlet.plugin.XMLElementAnnotation;
import org.beanlet.plugin.ElementAnnotationFactory;
import org.beanlet.BeanletValidationException;
import static org.beanlet.common.BeanletConstants.*;
import org.beanlet.annotation.AnnotationValueResolver;
import org.beanlet.annotation.AnnotationProxy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.beanlet.annotation.ConstructorElement;
import org.beanlet.annotation.ConstructorParameterElement;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.FieldElement;
import org.beanlet.annotation.MethodElement;
import org.beanlet.annotation.MethodParameterElement;
import org.beanlet.annotation.PackageElement;
import org.beanlet.annotation.TypeElement;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Leon van Zantvoort
 */
public abstract class AbstractElementAnnotationFactory<T extends Annotation>
        implements ElementAnnotationFactory<T> {
    
    private static final XPath xpath;
    private static final XPathExpression ALL_EXPRESSION;
    private static final XPathExpression PACKAGE_ATTRIBUTE_EXPRESSION;
    private static final XPathExpression FIELD_ATTRIBUTE_EXPRESSION;
    private static final XPathExpression FIELD_ELEMENT_EXPRESSION;
    private static final XPathExpression METHOD_ATTRIBUTE_EXPRESSION;
    private static final XPathExpression METHOD_INDEX_EXPRESSION;
    private static final XPathExpression METHOD_ELEMENT_EXPRESSION;
    private static final XPathExpression METHOD_PARAMETER_ELEMENT_EXPRESSION;
    private static final XPathExpression METHOD_PARAMETER_INDEX_EXPRESSION;
    private static final XPathExpression METHOD_PARAMETERS_EXPRESSION;
    private static final XPathExpression CONSTRUCTOR_ATTRIBUTE_EXPRESSION;
    private static final XPathExpression CONSTRUCTOR_INDEX_EXPRESSION;
    private static final XPathExpression CONSTRUCTOR_ELEMENT_EXPRESSION;
    private static final XPathExpression CONSTRUCTOR_PARAMETER_ELEMENT_EXPRESSION;
    private static final XPathExpression CONSTRUCTOR_PARAMETER_INDEX_EXPRESSION;
    private static final XPathExpression CONSTRUCTOR_PARAMETERS_EXPRESSION;
    
    private static final XPathExpression PARAMETER_TYPE_EXPRESSION;
    
    static {
        xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(BEANLET_NAMESPACE_CONTEXT);
        try {
            ALL_EXPRESSION = xpath.compile("./*");
            PACKAGE_ATTRIBUTE_EXPRESSION = xpath.compile("../@package");
            FIELD_ATTRIBUTE_EXPRESSION = xpath.compile("@field");
            FIELD_ELEMENT_EXPRESSION = xpath.compile("./:field/@name");
            METHOD_ATTRIBUTE_EXPRESSION = xpath.compile("@method");
            METHOD_INDEX_EXPRESSION = xpath.compile("../@index");
            METHOD_ELEMENT_EXPRESSION = xpath.compile("./:method/@name");
            METHOD_PARAMETER_ELEMENT_EXPRESSION = xpath.compile("./:method-parameter/@name");
            METHOD_PARAMETER_INDEX_EXPRESSION = xpath.compile("../@index");
            CONSTRUCTOR_ATTRIBUTE_EXPRESSION = xpath.compile("@constructor");
            CONSTRUCTOR_INDEX_EXPRESSION = xpath.compile("../@index");
            CONSTRUCTOR_ELEMENT_EXPRESSION = xpath.compile("./:constructor");
            CONSTRUCTOR_PARAMETER_ELEMENT_EXPRESSION = xpath.compile("./:constructor-parameter");
            CONSTRUCTOR_PARAMETER_INDEX_EXPRESSION = xpath.compile("@index");
            CONSTRUCTOR_PARAMETERS_EXPRESSION = xpath.compile("./:parameters");
            METHOD_PARAMETERS_EXPRESSION = xpath.compile("../:parameters");
            PARAMETER_TYPE_EXPRESSION = xpath.compile("./:parameter/@type");
        } catch (XPathExpressionException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Returns the annotation type supported by this factory.
     */
    public abstract Class<T> annotationType();
    
    /**
     * Returns the name of the xml element supported by this factory.
     */
    public abstract String getNodeName();
    
    /**
     * Returns the URI of the namespace supported by this factory, or
     * {@code null} if this factory is not namespace aware.
     */
    public abstract String getNamespaceURI();
    
    /**
     * This method is called if multiple elements are found for a given node.
     * Default implementation returns {@code true}
     */
    public boolean isMatch(Element element, T annotation) {
        return true;
    }
    
    public PackageElement getPackageElement(Node node, Package pkg) {
        try {
            Node value = (Node) PACKAGE_ATTRIBUTE_EXPRESSION.evaluate(node, 
                    XPathConstants.NODE);
            if (value == null || pkg.getName().equals(value.getNodeValue())) {
                return PackageElement.instance(pkg);
            }
        } catch (XPathExpressionException e) {
            assert false : e;
        }
        return null;
    }
    
    public List<? extends Element> getElements(String beanletName, Node node,
            Class<?> cls) {
        QName q = XPathConstants.NODE;
        try {
            // Duplicate element restriction should be enforced by schema.
            
            Node fieldNode = (Node) FIELD_ATTRIBUTE_EXPRESSION.evaluate(node, q);
            if (fieldNode == null) {
                fieldNode = (Node) FIELD_ELEMENT_EXPRESSION.evaluate(node, q);
            }
            if (fieldNode != null) {
                return getFieldElements(beanletName, fieldNode, cls);
            }
            
            Node methodNode = (Node) METHOD_ATTRIBUTE_EXPRESSION.evaluate(node, q);
            if (methodNode == null) {
                methodNode = (Node) METHOD_ELEMENT_EXPRESSION.evaluate(node, q);
            }
            if (methodNode != null) {
                return getMethodElements(beanletName, methodNode, cls);
            }
            
            Node methodParameterNode = (Node) METHOD_PARAMETER_ELEMENT_EXPRESSION.evaluate(node, q);
            if (methodParameterNode != null) {
                return getMethodParameterElements(beanletName,
                        methodParameterNode, cls);
            }
            
            Node constructorNode = (Node) CONSTRUCTOR_ATTRIBUTE_EXPRESSION.evaluate(node, q);
            if (constructorNode != null &&
                    !Boolean.valueOf(constructorNode.getNodeValue())) {
                constructorNode = null;
            }
            if (constructorNode == null) {
                constructorNode = (Node) CONSTRUCTOR_ELEMENT_EXPRESSION.evaluate(node, q);
            }
            if (constructorNode != null) {
                return getConstructorElements(beanletName, constructorNode, cls);
            }
            
            Node constructorParameterNode = (Node) CONSTRUCTOR_PARAMETER_ELEMENT_EXPRESSION.evaluate(node, q);
            if (constructorParameterNode != null) {
                return getConstructorParameterElements(beanletName,
                        constructorParameterNode, cls);
            }
            
            return Arrays.asList(TypeElement.instance(cls));
        } catch (XPathExpressionException e) {
            assert false : e;
        }
        return Collections.emptyList();
    }
    
    /**
     * @param elementName name of the element as specified by the xml
     * element. Upper case characters are replaced by a dash followed by the
     * lower case character.
     */
    public String getMappedName(String elementName) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < elementName.length(); i++) {
            char c = elementName.charAt(i);
            if (Character.isUpperCase(c)) {
                builder.append("-");
                builder.append(Character.toLowerCase(c));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }
    
    /**
     * Extracts the annotation member value from xml. Return {@code null} to
     * use default method value.
     * 
     * @param node xml annotation node.
     * @param elementName mapped xml element name.
     * @param type type of the annotation member.
     * @param parentValue value specified by parent annotation.
     * @param ctx
     * @return value from xml, to use parent or default method value.
     */
    public Object getValueFromNode(Node node, String elementName, Class type,
            Object parentValue, ElementAnnotationContext ctx) throws Throwable {
        Node n = (Node) xpath.evaluate("@" + elementName, node, XPathConstants.NODE);
        Object v = null;
        if (n != null) {
            v = valueOf(n.getNodeValue(), type, ctx.getClassLoader());
        }
        if (v == null) {
            v = parentValue;
        }
        return v;
    }
    
    public XMLElementAnnotation<PackageElement, T> getElementAnnotation(
            final Node node, Package pkg, ElementAnnotationContext ctx) {
        final PackageElement e = getPackageElement(node, pkg);
        if (e != null) {
            if (getNamespaceAwareNodeName(node).equals(getNodeName()) &&
                    (getNamespaceURI() == null ||
                    getNamespaceURI().equals(node.getNamespaceURI()))) {
                T p = null;
                if (ctx.getAnnotationDomain() != null) {
                    p = ctx.getAnnotationDomain().
                            getDeclaration(annotationType()).getAnnotation(e);
                }
                final T t = AnnotationProxy.newProxyInstance(annotationType(),
                        ctx.getClassLoader(), 
                        new AnnotationValueResolverImpl(node, p, ctx), true);
                return new XMLElementAnnotation<PackageElement, T>() {
                    public Node getNode() {
                        return node;
                    }
                    public PackageElement getElement() {
                        return e;
                    }
                    public T getAnnotation() {
                        return t;
                    }
                    public String toString() {
                        return "{element=" + getElement() + ", annotation=" +
                                getAnnotation() + "}";
                    }
                };
            }
        }
        return null;
    }
    
    public XMLElementAnnotation<Element, T> getElementAnnotation(
            final Node node, Class<?> cls, ElementAnnotationContext ctx) {
        if (getNamespaceAwareNodeName(node).equals(getNodeName()) &&
                (getNamespaceURI() == null ||
                getNamespaceURI().equals(node.getNamespaceURI()))) {
            List<XMLElementAnnotation<Element, T>> matched =
                    new ArrayList<XMLElementAnnotation<Element, T>>();
            List<? extends Element> elements = getElements(ctx.getBeanletName(), 
                    node, cls);
            for (final Element e : elements) {
                T p = null;
                if (ctx.getAnnotationDomain() != null) {
                    p = ctx.getAnnotationDomain().
                            getDeclaration(annotationType()).getAnnotation(e);
                }
                final T t = AnnotationProxy.newProxyInstance(annotationType(),
                        ctx.getClassLoader(), 
                        new AnnotationValueResolverImpl(node, p, ctx), true);
                if (elements.size() > 1 && !isMatch(e, t)) {
                    // Only verify if more than one elements are found.
                    continue;
                }
                
                matched.add(new XMLElementAnnotation<Element, T>() {
                    public Node getNode() {
                        return node;
                    }
                    public Element getElement() {
                        return e;
                    }
                    public T getAnnotation() {
                        return t;
                    }
                    public String toString() {
                        return "{element=" + getElement() + ", annotation=" +
                                getAnnotation() + "}";
                    }
                });
            }
            
            if (matched.isEmpty()) {
                if (elements.isEmpty()) {
                    throw new BeanletValidationException(ctx.getBeanletName(), 
                            "No element found for '" + toString(node, 2) + "' at " + cls + ".");
                } else {
                    throw new BeanletValidationException(ctx.getBeanletName(), 
                            "Elements found for '" + toString(node, 2) + "', " +
                            "but none match annotation: " + elements + ".");
                }
            } else if (matched.size() > 1) {
                List<Element> list = new ArrayList<Element>();
                for (XMLElementAnnotation e : matched) {
                    list.add(e.getElement());
                }
                throw new BeanletValidationException(ctx.getBeanletName(),
                        "Ambiguous elements specified for '" + toString(node, 2) + 
                        "': " + list + ".");
            } else {
                return matched.get(0);
            }
        }
        return null;
    }
    
    private List<? extends Element> getFieldElements(final String beanletName, 
            final Node node, final Class<?> cls) {
        return AccessController.doPrivileged(new PrivilegedAction<List<? extends Element>>() {
            public List<? extends Element> run() {
                List<FieldElement> elements = new ArrayList<FieldElement>();
                Class<?> tmp = cls;
                do {
                    // PERMISSION: java.lang.RuntimePermission accessDeclaredMembers
                    for (Field f : tmp.getDeclaredFields()) {
                        if (f.getName().equals(node.getNodeValue())) {
                            FieldElement element = FieldElement.instance(f);
                            assert !element.isHidden(cls);
                            elements.add(element);
                            break;
                        }
                    }
                } while (elements.isEmpty() && (tmp = tmp.getSuperclass()) != null);
                assert elements.size() <= 1;
                return elements;
            }
        });
    }
    
    private List<? extends Element> getMethodElements(
            String beanletName, Node node, final Class<?> cls) {
        final List<Element> elements =
                new ArrayList<Element>();
        try {
            final String methodName = node.getNodeValue();
            final String[] parameters;
            Node parametersNode = (Node) METHOD_PARAMETERS_EXPRESSION.evaluate(node,
                    XPathConstants.NODE);
            if (parametersNode != null) {
                NodeList list = (NodeList) PARAMETER_TYPE_EXPRESSION.evaluate(
                        parametersNode, XPathConstants.NODESET);
                parameters = new String[list.getLength()];
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = list.item(i).getNodeValue();
                }
            } else {
                parameters = null;
            }
            
            final Integer index;
            Node indexNode = (Node) METHOD_INDEX_EXPRESSION.evaluate(node, 
                    XPathConstants.NODE);
            if (indexNode != null) {
                try {
                    index = new Integer(indexNode.getNodeValue());
                } catch (NumberFormatException e) {
                    throw new BeanletValidationException(beanletName,
                            "Failed to parse index: '" + methodName + "'.", e);
                }
            } else {
                index = null;
            }
            
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    Class<?> tmp = cls;
                    do {
                        // PERMISSION: java.lang.RuntimePermission accessDeclaredMembers
                        for (Method m : tmp.getDeclaredMethods()) {
                            if (m.getName().equals(methodName)) {
                                final Element element;
                                if (index == null) {
                                    element = MethodElement.instance(m);
                                } else {
                                    if (index < m.getParameterTypes().length) {
                                        element = MethodParameterElement.instance(m, index);
                                    } else {
                                        continue;
                                    }
                                }
                                if (!element.isHidden(cls)) {
                                    if (parameters != null) {
                                        if (isExactMatch(m.getParameterTypes(), parameters)) {
                                            assert !element.isOverridden(cls);
                                            elements.add(element);
                                        }
                                    } else {
                                        if (!element.isOverridden(cls)) {
                                            elements.add(element);
                                        }
                                    }
                                }
                            }
                        }
                    } while ((parameters == null || elements.isEmpty()) &&
                            (tmp = tmp.getSuperclass()) != null);
                    return null;
                }
            });
        } catch (XPathExpressionException e) {
            assert false : e;
        }
        return elements;
    }
    
    private List<? extends Element> getMethodParameterElements(
            String beanletName, Node node, final Class<?> cls) {
        final List<MethodParameterElement> elements =
                new ArrayList<MethodParameterElement>();
        try {
            final String methodName = node.getNodeValue();
            final String[] parameters;
            Node parametersNode = (Node) METHOD_PARAMETERS_EXPRESSION.evaluate(node,
                    XPathConstants.NODE);
            if (parametersNode != null) {
                NodeList list = (NodeList) PARAMETER_TYPE_EXPRESSION.evaluate(
                        parametersNode, XPathConstants.NODESET);
                parameters = new String[list.getLength()];
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = list.item(i).getNodeValue();
                }
            } else {
                parameters = null;
            }
            
            final int index;
            Node indexNode = (Node) METHOD_PARAMETER_INDEX_EXPRESSION.
                    evaluate(node, XPathConstants.NODE);
            assert indexNode != null;
            try {
                index = Integer.parseInt(indexNode.getNodeValue());
            } catch (NumberFormatException e) {
                throw new BeanletValidationException(beanletName,
                        "Failed to parse index: '" + methodName + "'.", e);
            }
            
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    Class<?> tmp = cls;
                    do {
                        // PERMISSION: java.lang.RuntimePermission accessDeclaredMembers
                        for (Method m : tmp.getDeclaredMethods()) {
                            if (m.getName().equals(methodName)) {
                                MethodElement element = MethodElement.
                                        instance(m);
                                if (!element.isHidden(cls)) {
                                    if (parameters != null) {
                                        if (isExactMatch(m.getParameterTypes(), parameters)) {
                                            assert !element.isOverridden(cls);
                                            elements.add(MethodParameterElement.
                                                    instance(m, index));
                                        }
                                    } else {
                                        if (!element.isOverridden(cls)) {
                                            elements.add(MethodParameterElement.
                                                    instance(m, index));
                                        }
                                    }
                                }
                            }
                        }
                    } while ((parameters == null || elements.isEmpty()) &&
                            (tmp = tmp.getSuperclass()) != null);
                    return null;
                }
            });
        } catch (XPathExpressionException e) {
            assert false : e;
        }
        return elements;
    }
    
    private List<? extends Element> getConstructorElements(String beanletName,
            Node node, final Class<?> cls) {
        final List<Element> elements =
                new ArrayList<Element>();
        try {
            final String[] parameters;
            Node parametersNode = (Node) CONSTRUCTOR_PARAMETERS_EXPRESSION.
                    evaluate(node, XPathConstants.NODE);
            if (parametersNode != null) {
                NodeList list = (NodeList) PARAMETER_TYPE_EXPRESSION.evaluate(
                        parametersNode, XPathConstants.NODESET);
                parameters = new String[list.getLength()];
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = list.item(i).getNodeValue();
                }
            } else {
                parameters = null;
            }
            
            final Integer index;
            Node indexNode = (Node) CONSTRUCTOR_INDEX_EXPRESSION.evaluate(node, 
                    XPathConstants.NODE);
            if (indexNode != null) {
                try {
                    index = new Integer(indexNode.getNodeValue());
                } catch (NumberFormatException e) {
                    throw new BeanletValidationException(beanletName,
                            "Failed to parse constructor index: '" +
                            indexNode.getNodeValue() + "'.", e);
                }
            } else {
                index = null;
            }
            
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    // PERMISSION: java.lang.RuntimePermission accessDeclaredMembers
                    for (Constructor c : cls.getDeclaredConstructors()) {
                        final Element element;
                        if (index == null) {
                            element = ConstructorElement.instance(c);
                        } else {
                            if (index < c.getParameterTypes().length) {
                                element = ConstructorParameterElement.instance(c, index);
                            } else {
                                continue;
                            }
                        }
                        if (!element.isHidden(cls)) {
                            if (parameters != null) {
                                if (isExactMatch(c.getParameterTypes(), parameters)) {
                                    elements.add(element);
                                }
                            } else {
                                elements.add(element);
                            }
                        }
                    }
                    return null;
                }
            });
        } catch (XPathExpressionException e) {
            assert false : e;
        }
        return elements;
    }
    
    private List<? extends Element> getConstructorParameterElements(
            String beanletName, Node node, final Class<?> cls) {
        final List<ConstructorParameterElement> elements =
                new ArrayList<ConstructorParameterElement>();
        try {
            final String[] parameters;
            Node parametersNode = (Node) CONSTRUCTOR_PARAMETERS_EXPRESSION.
                    evaluate(node, XPathConstants.NODE);
            if (parametersNode != null) {
                NodeList list = (NodeList) PARAMETER_TYPE_EXPRESSION.evaluate(
                        parametersNode, XPathConstants.NODESET);
                parameters = new String[list.getLength()];
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = list.item(i).getNodeValue();
                }
            } else {
                parameters = null;
            }
            
            final int index;
            Node indexNode = (Node) CONSTRUCTOR_PARAMETER_INDEX_EXPRESSION.
                    evaluate(node, XPathConstants.NODE);
            assert indexNode != null;
            try {
                index = Integer.parseInt(indexNode.getNodeValue());
            } catch (NumberFormatException e) {
                throw new BeanletValidationException(beanletName,
                        "Failed to parse index: '" + indexNode.getNodeValue() +
                        "'.", e);
            }

            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    // PERMISSION: java.lang.RuntimePermission accessDeclaredMembers
                    for (Constructor c : cls.getDeclaredConstructors()) {
                        ConstructorElement element = ConstructorElement.
                                instance(c);
                        assert !element.isHidden(cls);
                        if (parameters != null) {
                            if (isExactMatch(c.getParameterTypes(), parameters)) {
                                elements.add(ConstructorParameterElement.
                                        instance(c, index));
                            }
                        } else {
                            elements.add(ConstructorParameterElement.
                                        instance(c, index));
                        }
                    }
                    return null;
                }
            });
        } catch (XPathExpressionException e) {
            assert false : e;
        }
        return elements;
    }
    
    private boolean isExactMatch(Class<?>[] memberParameterTypes,
            String[] xmlParameterTypes) {
        if (memberParameterTypes.length != xmlParameterTypes.length) {
            return false;
        }
        boolean match = true;
        for (int i = 0; i < memberParameterTypes.length; i++) {
            if (!memberParameterTypes[i].getCanonicalName().
                    equals(xmlParameterTypes[i])) {
                match = false;
                break;
            }
        }
        return match;
    }
    
    private static String getNamespaceAwareNodeName(Node node) {
        // PENDING: find a better way to remove namespace prefix.
        String nodeName = node.getNodeName();
        int ix = nodeName.indexOf(":");
        if (ix != -1) {
            nodeName = nodeName.substring(ix + 1);
        }
        return nodeName;
    }
    
    private class AnnotationValueResolverImpl implements
            AnnotationValueResolver {
        
        private final Node node;
        private final T parent;
        private final ElementAnnotationContext ctx;
        
        public AnnotationValueResolverImpl(Node node, T parent, 
                ElementAnnotationContext ctx) {
            this.node = node;
            this.parent = parent;
            this.ctx = ctx;
        }
        
        public Object getValue(Method method, ClassLoader loader) throws
                Throwable {
            String mappedName = getMappedName(method.getName());
            try {
                Object parentValue = null;
                if (parent != null) {
                    parentValue = method.invoke(parent);
                }
                Object v = getValueFromNode(node, mappedName,
                        method.getReturnType(), parentValue, ctx);
                if (v == null) {
                    v = method.getDefaultValue();
                }
                return v;
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
    
    public static String toString(Node node, int level) {
        if (level == 0) {
            return "<!-- ... -->";
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("<" + node.getNodeName());
            NamedNodeMap map = node.getAttributes();
            for (int i = 0; i < map.getLength(); i++) {
                Node n = map.item(i);
                builder.append(" ");
                builder.append(n.getNodeName());
                builder.append("=\"");
                builder.append(n.getNodeValue());
                builder.append("\"");
            }
            NodeList list = (NodeList) ALL_EXPRESSION.evaluate(node, 
                    XPathConstants.NODESET);
            if (list.getLength() > 0) {
                builder.append(">");
                if (level > 1) {
                    for (int i = 0; i < list.getLength(); i++) {
                        builder.append(toString(list.item(i), level - 1));
                    }
                } else {
                    builder.append("<!-- ... -->");
                }
                builder.append("</" + node.getNodeName() + ">");
            } else {
                builder.append("/>");
            }
            return builder.toString();
        } catch (XPathExpressionException ex) {
            return "<!-- Cannot parse " + node.getNodeName() + ". -->";
        }
    }
    
    /**
     * The default implementation of this method is restricted to 
     * {@code String}s, primitives, arrays of primitives,
     * {@code Class}es, arrays of {@code Class}es,
     * {@code Enum}s and arrays of {@code Enum}s
     */
    public static Object valueOf(String str, Class type,
            ClassLoader loader) throws Throwable {
        final Object o;
        if (type.isPrimitive()) {
            final Class<?> objectType;
            if (Boolean.TYPE.equals(type)) {
                objectType = Boolean.class;
            } else if (Byte.TYPE.equals(type)) {
                objectType = Byte.class;
            } else if (Short.TYPE.equals(type)) {
                objectType = Short.class;
            } else if (Integer.TYPE.equals(type)) {
                objectType = Integer.class;
            } else if (Long.TYPE.equals(type)) {
                objectType = Long.class;
            } else if (Float.TYPE.equals(type)) {
                objectType = Float.class;
            } else if (Double.TYPE.equals(type)) {
                objectType = Double.class;
            } else {
                throw new AssertionError(type);
            }
            try {
                o = str.length() == 0 ? null : objectType.getMethod(
                        "valueOf", String.class).invoke(null, str);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        } else if (type.isEnum()) {
            if (str.length() != 0) {
                Object tmp = null;
                try {
                    tmp = Enum.class.getMethod("valueOf",
                            Class.class, String.class).invoke(null, type, str);
                } catch (InvocationTargetException e) {
                    try {
                        throw e.getTargetException();
                    } catch (RuntimeException e2) {
                        throw e2;
                    } catch (Exception e2) {
                        assert false : e;
                    }
                }
                o = tmp;
            } else {
                o = null;
            }
        } else if (String.class.equals(type)) {
            o = str;
        } else if (Class.class.equals(type)) {
            o = loader.loadClass(str);
        } else if (type.isAnnotation()) {
            o = null;
        } else if (type.isArray()) {
            String[] list = str.split("\"(?<!\\\\), ");
            Class<?> componentType = type.getComponentType();
            if (componentType.isPrimitive() ||
                    componentType.isEnum() || 
                    componentType.equals(Class.class) ||
                    componentType.equals(String.class)) {
                int len = list.length;
                o = Array.newInstance(componentType, len);
                for (int i = 0; i < len; i++) {
                    ((Object[]) o)[i] = valueOf(list[i], componentType,
                            loader);
                }
            } else {
                o = null;
            }
        } else {
            o = null;
        }
        return o;
    }
}
