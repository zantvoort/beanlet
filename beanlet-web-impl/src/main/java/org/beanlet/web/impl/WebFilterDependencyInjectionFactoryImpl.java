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
import org.beanlet.BeanletWiringException;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.TypeElement;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.DependencyInjection;
import org.beanlet.plugin.DependencyInjectionFactory;
import org.beanlet.plugin.Injectant;
import org.beanlet.web.WebFilter;
import org.jargo.ComponentContext;

import javax.servlet.*;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 *
 * @author Leon van Zantvoort
 */
public class WebFilterDependencyInjectionFactoryImpl implements DependencyInjectionFactory {

    private final BeanletConfiguration<?> configuration;

    public WebFilterDependencyInjectionFactoryImpl(
            BeanletConfiguration<?> configuration) {
        this.configuration = configuration;
    }

    public List<DependencyInjection> getConstructorDependencyInjections(Class<?> cls) {
        final TypeElement typeElement = TypeElement.instance(configuration.getType());
        final WebFilter annotation = configuration.getAnnotationDomain().getDeclaration(
                WebFilter.class).getAnnotation(typeElement);
        if (annotation != null && annotation.createFilter()) {
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
            DependencyInjection injection = new DependencyInjection() {
                public Element getTarget() {
                    return typeElement;
                }

                public boolean isOptional() {
                    return false;
                }

                public Set<String> getDependencies() throws BeanletWiringException {
                    return Collections.emptySet();
                }

                public Injectant<?> getInjectant(ComponentContext<?> ctx) throws BeanletWiringException {
                    if (servletContext == null) {
                        throw new BeanletWiringException(ctx.getComponentMetaData().getComponentName(),
                                typeElement.getMember(), "ServletContext not available.");
                    }
                    @SuppressWarnings("unchecked")
                    Class<Filter> cls = (Class<Filter>) typeElement.getType();
                    try {
                        final Filter filter = servletContext.createFilter(cls);
                        return new Injectant<Object>() {
                            public boolean isCacheable() {
                                return true;
                            }

                            public Object getObject() {
                                return filter;
                            }
                        };
                    } catch (ServletException e) {
                        throw new BeanletWiringException(ctx.getComponentMetaData().getComponentName(),
                                typeElement.getMember(), e);
                    }
                }
            };
            return Arrays.asList(injection);
        } else {
            return Collections.emptyList();
        }
    }

    public List<DependencyInjection> getSetterDependencyInjections(Class<?> cls) {
        return Collections.emptyList();
    }

    public List<DependencyInjection> getFactoryDependencyInjections(Class<?> cls, String factoryMethod) {
        return Collections.emptyList();
    }
}