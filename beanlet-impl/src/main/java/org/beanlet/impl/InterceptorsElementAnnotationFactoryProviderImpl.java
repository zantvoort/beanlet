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
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import javax.xml.xpath.XPath;
import org.beanlet.Interceptors;
import org.beanlet.common.AbstractElementAnnotationFactory;
import org.beanlet.common.AbstractProvider;
import org.beanlet.common.BeanletConstants;
import org.beanlet.plugin.ElementAnnotationContext;
import org.beanlet.plugin.ElementAnnotationFactory;
import org.beanlet.plugin.spi.ElementAnnotationFactoryProvider;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Leon van Zantvoort
 */
public final class InterceptorsElementAnnotationFactoryProviderImpl extends 
        AbstractProvider implements ElementAnnotationFactoryProvider {
    
    public List<ElementAnnotationFactory> getElementAnnotationFactories() {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(BeanletConstants.BEANLET_NAMESPACE_CONTEXT);
        ElementAnnotationFactory factory = new AbstractElementAnnotationFactory() {
            public String getNamespaceURI() {
                return BEANLET_NAMESPACE_URI;
            }
            public String getNodeName() {
                return "interceptors";
            }
            public Class<? extends Annotation> annotationType() {
                return Interceptors.class;
            }
            public Object getValueFromNode(Node node, String elementName, 
                    Class type, Object parentValue, 
                    ElementAnnotationContext ctx) throws Throwable {
                if (elementName.equals("value")) {
                    NamedNodeMap map = node.getAttributes();
                    Node typeNode = map.getNamedItem("type");
                    if (typeNode != null) {
                        return new Class<?>[] {ctx.getClassLoader().
                                    loadClass(typeNode.getNodeValue())};
                    } else {
                        NodeList list = (NodeList) xpath.evaluate(
                                "./:class/@type", node, XPathConstants.NODESET);
                        Class<?>[] classes = new Class[list.getLength()];
                        for (int i = 0; i < list.getLength(); i++) {
                            classes[i] = ctx.getClassLoader().
                                    loadClass(list.item(i).getNodeValue());
                        }
                        return classes;
                    }
                } else {
                    return super.getValueFromNode(node, elementName, type, 
                            parentValue, ctx);
                }
            }
        };
        return Collections.singletonList(factory);
    }
}
