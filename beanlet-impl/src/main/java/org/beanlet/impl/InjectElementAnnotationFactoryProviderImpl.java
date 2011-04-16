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
import org.beanlet.annotation.FieldElement;
import org.beanlet.annotation.MethodParameterElement;
import java.lang.reflect.InvocationTargetException;
import org.beanlet.annotation.AnnotationValueResolver;
import org.beanlet.annotation.AnnotationProxy;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPath;
import org.beanlet.CollectionValue;
import org.beanlet.Entry;
import org.beanlet.Inject;
import org.beanlet.MapValue;
import org.beanlet.Value;
import org.beanlet.annotation.ConstructorElement;
import org.beanlet.annotation.ConstructorParameterElement;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.MethodElement;
import org.beanlet.common.AbstractElementAnnotationFactory;
import org.beanlet.common.AbstractProvider;
import org.beanlet.common.BeanletConstants;
import org.beanlet.plugin.ElementAnnotationContext;
import org.beanlet.plugin.ElementAnnotationFactory;
import org.beanlet.plugin.NestedBeanletFactory;
import org.beanlet.plugin.spi.ElementAnnotationFactoryProvider;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Leon van Zantvoort
 */
public final class InjectElementAnnotationFactoryProviderImpl extends 
        AbstractProvider implements ElementAnnotationFactoryProvider {
    
    public List<ElementAnnotationFactory> getElementAnnotationFactories() {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(BeanletConstants.BEANLET_NAMESPACE_CONTEXT);
        
        ElementAnnotationFactory factory = 
                new AbstractElementAnnotationFactory<Inject>() {
            public String getNamespaceURI() {
                return BEANLET_NAMESPACE_URI;
            }
            public String getNodeName() {
                return "inject";
            }
            public Class<Inject> annotationType() {
                return Inject.class;
            }
            public boolean isMatch(Element element, Inject annotation) {
                final Class<?> destinationType;
                final Class<?> sourceType;
                
                if (element instanceof FieldElement) {
                    if (Modifier.isStatic(element.getMember().getModifiers())) {
                        return true;
                    } else {
                        destinationType = ((FieldElement) element).getField().
                                getType();
                    }
                } else if (element instanceof MethodElement) {
                    Class<?>[] types = ((MethodElement) element).getMethod().
                            getParameterTypes();
                    if (types.length == 0) {
                        if (Modifier.isStatic(element.getMember().getModifiers())) {
                            return true;
                        } else {
                            return false;
                        }
                    } else if (types.length == 1) {
                        destinationType = types[0];
                    } else {
                        return false;
                    }
                } else if (element instanceof MethodParameterElement) {
                    // PENDING: add check.
                    Class<?>[] types = ((MethodParameterElement) element).
                            getMethod().getParameterTypes();
                    destinationType = types[((MethodParameterElement) element).
                            getParameter()];
                } else if (element instanceof ConstructorElement) {
                    Class<?>[] types = ((ConstructorElement) element).
                            getConstructor().getParameterTypes();
                    if (types.length == 1) {
                        destinationType = types[0];
                    } else {
                        return true;
                    }
                } else if (element instanceof ConstructorParameterElement) {
                    // PENDING: add check.
                    Class<?>[] types = ((ConstructorParameterElement) element).
                            getConstructor().getParameterTypes();
                    destinationType = types[((ConstructorParameterElement) element).
                            getParameter()];
                } else {
                    return false;
                }
                assert destinationType != null;
                Value value = annotation.value();
                final Class<?> tmpSourceType;
                if (value.nill() || !value.ref().equals("")) {
                    tmpSourceType = null;
                } else if (!value.value().equals("") || value.empty()) {
                    tmpSourceType = null;
                } else {
                    CollectionValue collection = annotation.collection();
                    if (collection.value().length > 0 || collection.empty()) {
                        if ((Collection.class.isAssignableFrom(destinationType) ||
                                    destinationType.isArray()) &&
                                    collection.type().equals(ArrayList.class)) {
                            tmpSourceType = destinationType;
                        } else {
                            tmpSourceType = collection.type();
                        }
                    } else {
                        MapValue map = annotation.map();
                        if (map.value().length > 0 || map.empty()) {
                            if (Map.class.isAssignableFrom(destinationType) &&
                                    map.type().equals(HashMap.class)) {
                                tmpSourceType = destinationType;
                            } else {
                                tmpSourceType = map.type();
                            }
                        } else {
                            // Type not supported, it's now up to Value.type().
                            tmpSourceType = null;
                        }
                    }
                }
                if (tmpSourceType == null) {
                    if (value.type().equals(Object.class)) {
                        sourceType = tmpSourceType;
                    } else {
                        sourceType = value.type();
                    }
                } else {
                    sourceType = tmpSourceType;
                }
                if (sourceType == null) {
                    return true;
                } else {
                    return destinationType.isAssignableFrom(sourceType);
                }
            }
            public Object getValueFromNode(Node node, String elementName, 
                    Class type, final Object parentValue, 
                    final ElementAnnotationContext ctx) throws Throwable {
                if (elementName.equals("value")) {
                    Node n = (Node) xpath.evaluate(
                            "./:value", node, XPathConstants.NODE);
                    return AnnotationProxy.newProxyInstance(Value.class,
                            ctx.getClassLoader(), new ValueAnnotationResolver(
                            n == null ? node : n, (Value) parentValue,
                            ctx.getNestedBeanletFactory()));
                } else if (elementName.equals("collection")) {
                    Node n = (Node) xpath.evaluate("./:collection", node, 
                            XPathConstants.NODE);
                    if (n == null) {
                        return null; // PENDING: or return parentValue instead?
                    }
                    Node t = (Node) xpath.evaluate("@type", n, 
                            XPathConstants.NODE);
                    if (t == null) {
                        t = (Node) xpath.evaluate("../@type", n, 
                                XPathConstants.NODE);
                    }
                            
                    final Node synced = n.getAttributes().getNamedItem("synced");
                    final Node unmodifiable = n.getAttributes().getNamedItem("unmodifiable");
                    final NodeList values = (NodeList) xpath.evaluate(
                            "./:value", n, XPathConstants.NODESET);
                    final Node collectionType = t;
                    AnnotationValueResolver resolver = 
                            new AnnotationValueResolver() {
                        public Object getValue(Method method, 
                                ClassLoader loader) throws Throwable {
                            try {
                                if (method.getName().equals("type")) {
                                    Object v = null;
                                    if (collectionType != null) {
                                        v = loader.loadClass(
                                                collectionType.getNodeValue());
                                    } else if (parentValue != null) {
                                        v = method.invoke(parentValue);
                                    }
                                    if (collectionType == null) {
                                        v = method.getDefaultValue();
                                    }
                                    return v;
                                } else if (method.getName().equals("empty")) {
                                    return values.getLength() == 0;
                                } else if (method.getName().equals("synced")) {
                                    return synced != null && Boolean.parseBoolean(
                                            synced.getNodeValue());
                                } else if (method.getName().equals("unmodifiable")) {
                                    return unmodifiable != null && Boolean.parseBoolean(
                                            unmodifiable.getNodeValue());
                                } else if (method.getName().equals("value")) {
                                    List<Value> l = new ArrayList<Value>();
                                    if (parentValue != null) {
                                        Value[] pv = (Value[]) method.
                                                invoke(parentValue);
                                        for (int i = 0; i < pv.length; i++) {
                                            l.add(pv[i]);
                                        }
                                    }
                                    for (int i = 0; i < values.getLength(); i++) {
                                        l.add(AnnotationProxy.
                                                newProxyInstance(Value.class, 
                                                loader, 
                                                new ValueAnnotationResolver(
                                                values.item(i), null, 
                                                ctx.getNestedBeanletFactory())));
                                    }
                                    return l.toArray(new Value[l.size()]);
                                } else {
                                    return method.getDefaultValue();
                                }
                            } catch (InvocationTargetException e) {
                                throw e.getTargetException();
                            }
                        }    
                    };
                    return AnnotationProxy.newProxyInstance(CollectionValue.class,
                            ctx.getClassLoader(), resolver);
                } else if (elementName.equals("map")) {
                    Node n = (Node) xpath.evaluate("./:map", node, 
                            XPathConstants.NODE);
                    if (n == null) {
                        return null; // PENDING: or return parentValue instead?
                    }
                    Node t = (Node) xpath.evaluate("@type", n, 
                            XPathConstants.NODE);
                    if (t == null) {
                        t = (Node) xpath.evaluate("../@type", n, 
                                XPathConstants.NODE);
                    }
                            
                    final Node synced = n.getAttributes().getNamedItem("synced");
                    final Node unmodifiable = n.getAttributes().getNamedItem("unmodifiable");
                    final NodeList entries = (NodeList) xpath.evaluate(
                            "./:entry", n, XPathConstants.NODESET);
                    final Node mapType = t;
                    AnnotationValueResolver resolver = 
                            new AnnotationValueResolver() {
                        public Object getValue(Method method, ClassLoader loader) throws Throwable {
                            if (method.getName().equals("type")) {
                                Object v = null;
                                if (mapType != null) {
                                    v = loader.loadClass(mapType.getNodeValue());
                                } else if (parentValue != null) {
                                    v = ((MapValue) parentValue).type();
                                }
                                if (mapType == null) {
                                    v = method.getDefaultValue();
                                }
                                return v;
                            } else if (method.getName().equals("empty")) {
                                return entries.getLength() == 0;
                            } else if (method.getName().equals("synced")) {
                                return synced != null && Boolean.parseBoolean(
                                        synced.getNodeValue());
                            } else if (method.getName().equals("unmodifiable")) {
                                return unmodifiable != null && Boolean.parseBoolean(
                                        unmodifiable.getNodeValue());
                            } else if (method.getName().equals("value")) {
                                List<Entry> l = new ArrayList<Entry>();
                                if (parentValue != null) {
                                    Entry[] pv = (Entry[]) method.
                                            invoke(parentValue);
                                    for (int i = 0; i < pv.length; i++) {
                                        l.add(pv[i]);
                                    }
                                }
                                for (int i = 0; i < entries.getLength(); i++) {
                                    final int ix = i;
                                    l.add(AnnotationProxy.
                                            newProxyInstance(Entry.class, loader, 
                                            new AnnotationValueResolver() {
                                        public Object getValue(Method method, ClassLoader loader) throws Throwable {
                                            final AnnotationValueResolver resolver;
                                            if (method.getName().equals("key")) {
                                                Node entryNode = entries.item(ix);
                                                NamedNodeMap map = entryNode.getAttributes();
                                                Node keyAttribute = map.getNamedItem("key");
                                                if (keyAttribute != null) {
                                                    resolver = new ValueAnnotationResolver(keyAttribute.getNodeValue());
                                                } else {
                                                    Node keyNode = (Node) xpath.evaluate("./:key", entryNode, XPathConstants.NODE);
                                                    if (keyNode != null) {
                                                        resolver = new ValueAnnotationResolver(keyNode, null, 
                                                                ctx.getNestedBeanletFactory());
                                                    } else {
                                                        resolver = new ValueAnnotationResolver();
                                                    }
                                                }                                                
                                            } else if (method.getName().equals("value")) {
                                                Node entryNode = entries.item(ix);
                                                NamedNodeMap map = entryNode.getAttributes();
                                                Node valueAttribute = map.getNamedItem("value");
                                                if (valueAttribute != null) {
                                                    resolver = new ValueAnnotationResolver(valueAttribute.getNodeValue());
                                                } else {
                                                    Node valueNode = (Node) xpath.evaluate("./:value", entryNode, XPathConstants.NODE);
                                                    if (valueNode != null) {
                                                        resolver = new ValueAnnotationResolver(valueNode, null, 
                                                                ctx.getNestedBeanletFactory());
                                                    } else {
                                                        resolver = new ValueAnnotationResolver();
                                                    }
                                                }                                                
                                            } else {
                                                throw new AssertionError(method);
                                            }
                                            return AnnotationProxy.
                                                newProxyInstance(Value.class, 
                                                    loader, resolver);
                                        }
                                    }));
                                }
                                return l.toArray(new Entry[l.size()]);
                            } else {
                                return method.getDefaultValue();
                            }
                        }    
                    };
                    return AnnotationProxy.newProxyInstance(MapValue.class,
                            ctx.getClassLoader(), resolver);
                } else {
                    return super.getValueFromNode(node, elementName, type, 
                            parentValue, ctx);
                }
            }
        };
        return Collections.singletonList(factory);
    }
    
    private static class ValueAnnotationResolver implements AnnotationValueResolver {
        
        private final String value;
        private final Node node;
        private final Value parentValue;
        private final XPath xpath;
        private final NestedBeanletFactory nestedFactory;
        
        public ValueAnnotationResolver() {
            this.value = null;
            this.node = null;
            this.parentValue = null;
            this.xpath = null;
            this.nestedFactory = null;
        }
        
        public ValueAnnotationResolver(String value) {
            this.value = value;
            this.node = null;
            this.parentValue = null;
            this.xpath = null;
            this.nestedFactory = null;
        }

        public ValueAnnotationResolver(Node node, Value parentValue,
                NestedBeanletFactory nestedFactory) {
            this.value = null;
            this.node = node;
            this.parentValue = parentValue;
            this.xpath = XPathFactory.newInstance().newXPath();
            this.nestedFactory = nestedFactory;
            xpath.setNamespaceContext(BeanletConstants.BEANLET_NAMESPACE_CONTEXT);
        }
        
        public Object getValue(Method method, ClassLoader loader) throws 
                Throwable {
            final Node n;
            if (value != null) {
                if (method.getName().equals("empty")) {
                    return value.equals("");
                } else if (method.getName().equals("value")) {
                    return value;
                }
                n = null;
            } else if (node == null) {
                if (method.getName().equals("nill")) {
                    return true;
                }
                n = null;
            } else {
                if (method.getName().equals("empty")) {
                    Node valueNode = (Node) xpath.evaluate("@value", node, 
                            XPathConstants.NODE);
                    if (valueNode != null && valueNode.getNodeValue().equals("")) {
                        return true;
                    } else if (parentValue != null) {
                        return parentValue.empty();
                    } else {
                        return method.getDefaultValue();
                    }
                } else if (method.getName().equals("ref")) {
                    Node beanletNode = (Node) xpath.evaluate("./:beanlet", node, 
                            XPathConstants.NODE);
                    if (beanletNode != null) {
                        return nestedFactory.create(beanletNode);
                    }
                }
                n = (Node) xpath.evaluate("@" + method.getName(), node, 
                        XPathConstants.NODE);
            }
            
            Object v = null;
            if (n != null) {
                v = AbstractElementAnnotationFactory.valueOf(n.getNodeValue(), 
                        method.getReturnType(), loader);
            }
            if (v == null && parentValue != null) {
                try {
                   v = method.invoke(parentValue);
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }
            if (v == null) {
                v = method.getDefaultValue();
            }
            return v;
        }
    }
}
