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

import org.beanlet.annotation.AnnotationDeclaration;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.TypeElement;
import org.beanlet.common.AbstractProvider;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.web.WebFilter;
import org.beanlet.web.WebListener;
import org.beanlet.web.WebServlet;
import org.jargo.ComponentConfiguration;
import org.jargo.ComponentObject;
import org.jargo.ComponentObjectBuilder;
import org.jargo.ComponentObjectFactory;
import org.jargo.spi.ComponentObjectFactoryProvider;

import javax.servlet.ServletContext;
import java.util.logging.Logger;

/**
 * @author Leon van Zantvoort
 */
public final class WebBeanletObjectFactoryProviderImpl extends
        AbstractProvider implements ComponentObjectFactoryProvider {

    private final Logger logger = Logger.getLogger(
            WebBeanletObjectFactoryProviderImpl.class.getName());

    public <T> ComponentObjectFactory<T> getComponentObjectFactory(
            ComponentConfiguration<T> configuration) {
        final ComponentObjectFactory<T> factory;
        if (WebConstants.isWebServletSupported()) {
            AnnotationDomain domain = ((BeanletConfiguration) configuration).getAnnotationDomain();
            TypeElement e = TypeElement.instance(configuration.getType());

            AnnotationDeclaration<WebServlet> s = domain.getDeclaration(WebServlet.class);
            AnnotationDeclaration<WebFilter> f = domain.getDeclaration(WebFilter.class);
            AnnotationDeclaration<WebListener> l = domain.getDeclaration(WebListener.class);

            if (s.getAnnotation(e) != null || f.getAnnotation(e) != null || l.getAnnotation(e) != null) {
                final ServletContext ctx = WebHelper.getServletContext();
                factory = new ComponentObjectFactory<T>() {
                    private ComponentObject<T> o;

                    public boolean isStatic() {
                        return true;
                    }

                    public void init(ComponentObjectBuilder<T> builder) {
                        o = builder.newInstance();
                    }

                    public ComponentObject<T> create() {
                        assert false;
                        return null;
                    }

                    public void remove() {
                        assert false;
                    }

                    public ComponentObject<T> getComponentObject() {
                        return o;
                    }

                    public void destroy() {
                        if (o != null) {
                            o.destroy();
                        }
                    }
                };
            } else {
                factory = null;
            }
        } else {
            factory = null;
        }
        return factory;
    }
}
