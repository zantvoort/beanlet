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

import org.beanlet.BeanletCreationException;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.TypeElement;
import org.beanlet.common.AbstractProvider;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.web.WebServlet;
import org.jargo.ComponentConfiguration;
import org.jargo.ComponentCreationException;
import org.jargo.ObjectFactory;
import org.jargo.ProxyGenerator;
import org.jargo.deploy.SequentialDeployable;
import org.jargo.spi.ObjectFactoryProvider;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.lang.reflect.Constructor;

/**
 *
 * @author Leon van Zantvoort
 */
public final class WebServletObjectFactoryProviderImpl extends AbstractProvider
        implements ObjectFactoryProvider {

    public Sequence sequence(SequentialDeployable deployable) {
        return Sequence.BEFORE;
    }

    public <T> ObjectFactory<T> getObjectFactory(
            final ComponentConfiguration<T> configuration) {
        final ObjectFactory<T> factory;
        if (WebConstants.isWebServletSupported()) {
            if (configuration instanceof BeanletConfiguration) {
                AnnotationDomain domain = ((BeanletConfiguration<?>) configuration).getAnnotationDomain();
                final TypeElement typeElement = TypeElement.instance(configuration.getType());
                final WebServlet annotation = domain.getDeclaration(
                        WebServlet.class).getAnnotation(typeElement);
                if (annotation != null && annotation.createServlet()) {
                    Constructor<?> c = null;
                    try {
                        c = configuration.getType().getConstructor();
                    } catch (NoSuchMethodException e) {
                    }
                    final Constructor<?> constructor = c;
                    if (constructor == null) {
                        throw new BeanletCreationException(configuration.getComponentName(),
                                "WebServlet MUST have a public zero-argument constructor.");
                    }
                    final ServletContext servletContext = WebHelper.getServletContext();
                    assert servletContext != null;
                    factory = new ObjectFactory<T>() {
                        @SuppressWarnings("unchecked")
                        public T newInstance() throws ComponentCreationException {
                            Class<Servlet> cls = (Class<Servlet>) typeElement.getType();
                            try {
                                return (T) servletContext.createServlet(cls);
                            } catch (ServletException e) {
                                throw new ComponentCreationException(configuration.getComponentName(), e);
                            }
                        }

                        public T newInstance(ProxyGenerator<T> proxyGenerator) throws ComponentCreationException {
                            return proxyGenerator.generateProxy(new Class[0]);
                        }
                    };
                } else {
                    factory = null;
                }
            } else {
                factory = null;
            }
        } else {
            factory = null;
        }
        return  factory;
    }
}
