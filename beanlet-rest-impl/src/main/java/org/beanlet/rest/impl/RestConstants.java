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
package org.beanlet.rest.impl;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 *
 * @author Leon van Zantvoort
 */
public final class RestConstants {

    public static final String REST_NAMESPACE_URI = "http://beanlet.org/schema/rest";

    public static final NamespaceContext REST_NAMESPACE_CONTEXT =
            new NamespaceContext() {
                public String getNamespaceURI(String prefix) {
                    return REST_NAMESPACE_URI;
                }
                public String getPrefix(String namespaceURI) {
                    return XMLConstants.DEFAULT_NS_PREFIX;
                }
                public Iterator getPrefixes(String namespaceURI) {
                    return Collections.singleton(
                            XMLConstants.DEFAULT_NS_PREFIX).iterator();
                }
            };

    private static final Logger logger = Logger.getLogger(
            RestConstants.class.getName());
    private static final boolean containerInitializerAvailable;
    private static final boolean contextListenerAvailable;

    static {
        boolean tmp = true;
        try {
            RestConstants.class.getClassLoader().
                    loadClass("javax.servlet.ServletContainerInitializer");
        } catch (ClassNotFoundException ex) {
            tmp = false;
        }
        containerInitializerAvailable = tmp;

        tmp = true;
        try {
            RestConstants.class.getClassLoader().
                    loadClass("javax.servlet.ServletContextListener");
        } catch (ClassNotFoundException ex) {
            tmp = false;
        }
        contextListenerAvailable = tmp;
        if (!contextListenerAvailable || !containerInitializerAvailable) {
            logger.info("Servlet API is not available. Restlet support disabled.");
        }
    }

    public static boolean isWebServletSupported() {
        return containerInitializerAvailable;
    }

    public static boolean isSupported() {
        return contextListenerAvailable;
    }

    private RestConstants() {
    }
}

