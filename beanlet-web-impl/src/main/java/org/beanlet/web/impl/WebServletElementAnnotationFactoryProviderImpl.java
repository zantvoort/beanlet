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
package org.beanlet.web.impl;

import org.beanlet.*;
import org.beanlet.annotation.*;
import org.beanlet.common.AbstractElementAnnotationFactory;
import org.beanlet.common.AbstractProvider;
import org.beanlet.common.BeanletConstants;
import org.beanlet.plugin.ElementAnnotationContext;
import org.beanlet.plugin.ElementAnnotationFactory;
import org.beanlet.plugin.NestedBeanletFactory;
import org.beanlet.plugin.spi.ElementAnnotationFactoryProvider;
import org.beanlet.web.WebServlet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.annotation.WebInitParam;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.beanlet.common.BeanletConstants.BEANLET_NAMESPACE_URI;

/**
 *
 * @author Leon van Zantvoort
 */
public final class WebServletElementAnnotationFactoryProviderImpl extends
        AbstractProvider implements ElementAnnotationFactoryProvider {

    public List<ElementAnnotationFactory> getElementAnnotationFactories() {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(WebConstants.WEB_NAMESPACE_CONTEXT);

        ElementAnnotationFactory factory =
                new AbstractElementAnnotationFactory<WebServlet>() {
            public String getNamespaceURI() {
                return WebConstants.WEB_NAMESPACE_URI;
            }
            public String getNodeName() {
                return "servlet";
            }
            public Class<WebServlet> annotationType() {
                return WebServlet.class;
            }
            public Object getValueFromNode(Node node, String elementName,
                    Class type, final Object parentValue,
                    ElementAnnotationContext ctx) throws Throwable {
                if (elementName.equals("url-patterns")) {
                    NodeList pattern = (NodeList) xpath.evaluate(
                            "./:url-pattern/@value", node, XPathConstants.NODESET);
                    NodeList patterns = (NodeList) xpath.evaluate(
                            "./:url-patterns/:url-pattern/@value", node, XPathConstants.NODESET);

                    if (pattern.getLength() == 1) {
                        return new String[]{pattern.item(0).getNodeValue()};
                    } else {
                        String[] s = new String[patterns.getLength()];
                        for (int i = 0; i < patterns.getLength(); i++) {
                            s[i] = patterns.item(i).getNodeValue();
                        }
                        return s;
                    }
                } else if (elementName.equals("init-params")) {
                    NodeList names = (NodeList) xpath.evaluate(
                            "./:init-param/@name", node, XPathConstants.NODESET);
                    NodeList values = (NodeList) xpath.evaluate(
                            "./:init-param/@value", node, XPathConstants.NODESET);
                    if (names.getLength() == 0) {
                        names = (NodeList) xpath.evaluate(
                                "./:init-params/:init-param/@name", node, XPathConstants.NODESET);
                        values = (NodeList) xpath.evaluate(
                                "./:init-params/:init-param/@value", node, XPathConstants.NODESET);
                    }

                    assert names.getLength() == values.getLength();

                    int len = names.getLength();
                    WebInitParam[] params = new WebInitParam[len];
                    for (int i = 0; i < len; i++) {
                        final String name = names.item(i).getNodeValue();
                        final String value = values.item(i).getNodeValue();
                        final String description;
                        Node d = (Node) xpath.evaluate("../@description", names.item(i),
                                XPathConstants.NODE);
                        if (d == null) {
                            description = "";
                        } else {
                            description = d.getNodeValue();
                        }

                        params[i] = AnnotationProxy.newProxyInstance(
                                WebInitParam.class, ctx.getClassLoader(),
                                new AnnotationValueResolver() {
                            public Object getValue(Method method, ClassLoader loader) throws Throwable {
                                final Object o;
                                if (method.getName().equals("name")) {
                                    o = name;
                                } else if (method.getName().equals("value")) {
                                    o = value;
                                } else if (method.getName().equals("description")) {
                                    o = description;
                                } else {
                                    o = method.getDefaultValue();
                                }
                                return o;
                            }
                        });
                    }
                    return params;
                } else {
                    return super.getValueFromNode(node, elementName, type,
                            parentValue, ctx);
                }
            }
        };
        return Collections.singletonList(factory);
    }
}
