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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.beanlet.BeanletDefinitionException;
import org.beanlet.plugin.ClassResolver;
import org.beanlet.plugin.ElementAnnotationFactory;
import org.beanlet.plugin.NestedBeanletFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author Leon van Zantvoort
 */
final class NestedBeanletFactoryImpl implements NestedBeanletFactory {
        
    private final String outerBeanletName;
    private final ClassLoader loader;
    private final ElementAnnotationFactory annotationFactory;
    private final ClassResolver resolver;
    private final Map<String, XMLAnnotationDomain<?>> map;

    private final Map<String, Node> nodes;

    private final AtomicBoolean inContext;

    public NestedBeanletFactoryImpl(String outerBeanletName, 
            ClassLoader loader, 
            ElementAnnotationFactory annotationFactory, 
            ClassResolver resolver, 
            Map<String, XMLAnnotationDomain<?>> map) {
        this.outerBeanletName = outerBeanletName;
        this.loader = loader;
        this.annotationFactory = annotationFactory;
        this.resolver = resolver;
        this.map = map;
        this.nodes = new LinkedHashMap<String, Node>();
        this.inContext = new AtomicBoolean(true);
    }

    /**
     * The name of the nested beanlet is simply the outer beanlet name with
     * a postfix. This postfix is the "$" character plus the name of the 
     * nested beanlet. If no name is specified, this name is replaced with
     * a number, making the entire name unique. Contrary to top level 
     * beanlets, the beanlet's type is not used to construct the postfix, as
     * this type might not be known at this moment.
     */
    public String create(Node beanletNode) {
        if (!inContext.get()) {
            throw new IllegalStateException(
                    "Nested beanlets can only be created while in scope of " +
                    "the ElementAnnotationFactory methods.");
        }
        NamedNodeMap attributes = beanletNode.getAttributes();
        Node nameNode = attributes.getNamedItem("name");
        String nestedBeanletName = nameNode == null ? null : nameNode.getNodeValue();
        if (nestedBeanletName == null) {
            nestedBeanletName = XMLAnnotationDomain.
                    getAnonymousBeanletName(outerBeanletName + "$");
        } else {
            if (nestedBeanletName.contains("$")) {
                throw new BeanletDefinitionException(nestedBeanletName,
                        "$ is a reserved character.");
            }
            nestedBeanletName = outerBeanletName + "$" + nestedBeanletName;
        }
        if (nodes.put(nestedBeanletName, beanletNode) != null) {
            throw new BeanletDefinitionException(nestedBeanletName,
                    "Duplicate declaration of beanlet.");
        }
        return nestedBeanletName;
    }

    public List<XMLAnnotationDomain<?>> get() {
        inContext.set(false);
        List<XMLAnnotationDomain<?>> list =
                new ArrayList<XMLAnnotationDomain<?>>();
        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            String beanletName = entry.getKey();
            Node beanletNode = entry.getValue();
            XMLAnnotationDomain<?> parentDomain = null;
            NamedNodeMap attributes = beanletNode.getAttributes();
            Node parentNode = attributes.getNamedItem("parent");
            if (parentNode != null) {
                String parent = parentNode.getNodeValue();
                parentDomain = map.get(parent);
                if (parentDomain == null) {
                    throw new BeanletDefinitionException(beanletName,
                            "Parent does not exist: '" + parent + "'.");
                }
            }
            // Add domain to map, so that it is available at nestedFactory.get.
            Map<String, XMLAnnotationDomain<?>> parentDomains = 
                    new HashMap<String, XMLAnnotationDomain<?>>(map);
            NestedBeanletFactoryImpl nestedFactory = 
                    new NestedBeanletFactoryImpl(beanletName, loader, 
                    annotationFactory, resolver, parentDomains);
            XMLAnnotationDomain<?> domain = XMLAnnotationDomain.createNestedDomain(beanletName, 
                    beanletNode, loader, annotationFactory, resolver, 
                    parentDomain, nestedFactory);
            map.put(beanletName, domain);
            parentDomains.put(beanletName, domain);
            String nestedBeanletName = beanletName.substring(
                    outerBeanletName.length() + 1);
            if (!nestedBeanletName.startsWith("$")) {
                map.put(nestedBeanletName, domain);
                parentDomains.put(nestedBeanletName, domain);
            }
            list.add(domain);
            list.addAll(nestedFactory.get());
        }
        return list;
    }
}
