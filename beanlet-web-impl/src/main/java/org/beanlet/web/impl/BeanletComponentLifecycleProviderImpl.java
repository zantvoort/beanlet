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
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.TypeElement;
import org.beanlet.common.AbstractProvider;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.Injectant;
import org.beanlet.web.WebFilter;
import org.beanlet.web.WebListener;
import org.beanlet.web.WebServlet;
import org.jargo.ComponentConfiguration;
import org.jargo.ComponentFactory;
import org.jargo.ComponentLifecycle;
import org.jargo.deploy.SequentialDeployable;
import org.jargo.spi.ComponentLifecycleProvider;

import javax.servlet.*;
import javax.servlet.annotation.WebInitParam;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * @author Leon van Zantvoort
 */
public class BeanletComponentLifecycleProviderImpl extends AbstractProvider implements ComponentLifecycleProvider {

    public <T> List<ComponentLifecycle<T>> getComponentLifecycles(final ComponentConfiguration<T> configuration, Executor executor) {
        List<ComponentLifecycle<T>> list = new ArrayList<ComponentLifecycle<T>>();
        if (configuration instanceof BeanletConfiguration) {
            final ServletContext servletContext = WebHelper.getServletContext();
            AnnotationDomain domain = ((BeanletConfiguration<?>) configuration).getAnnotationDomain();
            TypeElement typeElement = TypeElement.instance(configuration.getType());
            final WebServlet webServlet = domain.getDeclaration(WebServlet.class).getAnnotation(typeElement);
            if (webServlet != null) {
                list.add(new ComponentLifecycle<T>() {
                    public void onCreate(ComponentFactory<T> componentFactory) {
                        if (servletContext == null) {
                            throw new BeanletCreationException(configuration.getComponentName(), "ServletContext not available.");
                        }
                        Servlet servlet = (Servlet) componentFactory.create().getComponent();
                        ServletRegistration.Dynamic registration = servletContext.addServlet(webServlet.name(), servlet);
                        registration.addMapping(webServlet.value().length == 0 ? webServlet.urlPatterns() : webServlet.value());
                        Map<String, String> initParams = new HashMap<String, String>();
                        for (WebInitParam p : webServlet.initParams()) {
                            initParams.put(p.name(), p.value());
                        }
                        registration.setInitParameters(initParams);
                        registration.setAsyncSupported(webServlet.asyncSupported());
                        registration.setLoadOnStartup(webServlet.loadOnStartup());
                    }

                    public void onDestroy(ComponentFactory<T> componentFactory) {
                    }
                });
            }
            final WebFilter webFilter = domain.getDeclaration(WebFilter.class).getAnnotation(typeElement);
            if (webFilter != null) {
                list.add(new ComponentLifecycle<T>() {
                    public void onCreate(ComponentFactory<T> componentFactory) {
                        if (servletContext == null) {
                            throw new BeanletCreationException(configuration.getComponentName(), "ServletContext not available.");
                        }
                        Filter filter = (Filter) componentFactory.create().getComponent();
                        FilterRegistration.Dynamic registration = servletContext.addFilter(webFilter.filterName(), filter);
                        EnumSet<DispatcherType> dt = EnumSet.copyOf(Arrays.asList(webFilter.dispatcherTypes()));
                        registration.addMappingForServletNames(dt, true, webFilter.servletNames());
                        Map<String, String> initParams = new HashMap<String, String>();
                        for (WebInitParam p : webFilter.initParams()) {
                            initParams.put(p.name(), p.value());
                        }
                        registration.setInitParameters(initParams);
                        registration.setAsyncSupported(webFilter.asyncSupported());
                        registration.addMappingForUrlPatterns(dt, true, webFilter.urlPatterns());
                    }

                    public void onDestroy(ComponentFactory<T> componentFactory) {
                    }
                });
            }
            final WebListener webListener = domain.getDeclaration(WebListener.class).getAnnotation(typeElement);
            if (webListener != null) {
                list.add(new ComponentLifecycle<T>() {
                    public void onCreate(ComponentFactory<T> componentFactory) {
                        if (servletContext == null) {
                            throw new BeanletCreationException(configuration.getComponentName(), "ServletContext not available.");
                        }
                        EventListener listener = (EventListener) componentFactory.create().getComponent();
                        servletContext.addListener(listener);
                    }

                    public void onDestroy(ComponentFactory<T> componentFactory) {
                    }
                });
            }
        }
        return Collections.unmodifiableList(list);
    }
}
