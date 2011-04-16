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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import org.beanlet.BeanletValidationException;
import org.beanlet.impl.metadata.ExecuteMetaDataImpl;
import org.beanlet.common.AbstractBeanletProvider;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.common.Beanlets;
import org.beanlet.common.InvocationImpl;
import org.beanlet.common.event.ExecuteEventImpl;
import org.jargo.Event;
import org.jargo.ComponentReferenceLifecycle;
import org.beanlet.Execute;
import org.jargo.ComponentConfiguration;
import org.jargo.EventFactory;
import org.jargo.spi.EventFactoryProvider;
import org.jargo.InvocationFactory;
import org.jargo.spi.InvocationFactoryProvider;
import org.jargo.Invocation;
import org.jargo.ComponentLifecycle;
import org.jargo.MetaData;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.MethodElement;
import org.jargo.spi.ComponentLifecycleProvider;
import org.jargo.spi.ComponentReferenceLifecycleProvider;
import org.jargo.spi.MetaDataProvider;

/**
 * @author Leon van Zantvoort
 */
public final class ExecuteBeanletProviderImpl extends
        AbstractBeanletProvider implements InvocationFactoryProvider,
        EventFactoryProvider, ComponentLifecycleProvider,
        ComponentReferenceLifecycleProvider, MetaDataProvider {
    
    public Class<? extends Annotation> annotationType() {
        return Execute.class;
    }
    
    public Method getInterfaceMethod() {
        return null;
    }
    
    private Set<Class<? extends Event>> getEventTypes() {
        Set<Class<? extends Event>> types =
                new HashSet<Class<? extends Event>>();
        types.add(ExecuteEventImpl.class);
        return types;
    }
    
    public List<InvocationFactory> getInvocationFactories(
            ComponentConfiguration configuration) {
        List<InvocationFactory> factories =
                new ArrayList<InvocationFactory>();
        final Method method = getMethod(configuration);
        if (method != null) {
            final Set<Method> methods = Collections.singleton(method);
            Set<Class<? extends Event>> tmp =
                    new LinkedHashSet<Class<? extends Event>>();
            tmp.addAll(getEventTypes());
            final Set<Class<? extends Event>> eventTypes = Collections.
                    unmodifiableSet(tmp);
            final Invocation invocation = new InvocationImpl(method);
            factories.add(new InvocationFactory() {
                public Set<Method> getMethods() {
                    return methods;
                }
                public Set<Class<? extends Event>> getEventTypes() {
                    return eventTypes;
                }
                public Invocation getInvocation(Event event) {
                    return invocation;
                }
            });
        }
        return Collections.unmodifiableList(factories);
    }
    
    public List<EventFactory> getEventFactories(
            ComponentConfiguration configuration) {
        List<EventFactory> factories = new ArrayList<EventFactory>();
        if (getMethod(configuration) != null) {
            factories.add(new ExecuteEventFactoryImpl(configuration));
        }
        return Collections.unmodifiableList(factories);
    }
    
    public <T> List<ComponentLifecycle<T>> getComponentLifecycles(
            ComponentConfiguration<T> configuration, Executor executor) {
        List<ComponentLifecycle<T>> lifecycles =
                new ArrayList<ComponentLifecycle<T>>();
        Beanlets beanlets = Beanlets.getInstance(configuration);
        if (configuration instanceof BeanletConfiguration<?>) {
            AnnotationDomain domain = ((BeanletConfiguration<?>) configuration).
                    getAnnotationDomain();
            Method method = getMethod(configuration);
            if (method != null) {
                Execute execute = domain.getDeclaration(Execute.class).
                        getAnnotation(MethodElement.instance(method));
                check(execute, configuration.getComponentName());
                lifecycles.add(new ExecuteBeanletLifecycleImpl<T>(
                        executor, execute.threads(), execute.loop(),
                        execute.interrupt(), execute.join()));
            }
        }
        return Collections.unmodifiableList(lifecycles);
    }
    
    public <T> List<ComponentReferenceLifecycle<T>> getComponentReferenceLifecycles(
            ComponentConfiguration<T> configuration, Executor executor) {
        List<ComponentReferenceLifecycle<T>> lifecycles =
                new ArrayList<ComponentReferenceLifecycle<T>>();
        Beanlets beanlets = Beanlets.getInstance(configuration);
        if (configuration instanceof BeanletConfiguration<?>) {
            AnnotationDomain domain = ((BeanletConfiguration<?>) configuration).
                    getAnnotationDomain();
            Method method = getMethod(configuration);
            if (method != null) {
                Execute execute = domain.getDeclaration(Execute.class).
                        getAnnotation(MethodElement.instance(method));
                check(execute, configuration.getComponentName());
                lifecycles.add(new ExecuteBeanletLifecycleImpl<T>(
                        executor, execute.threads(), execute.loop(),
                        execute.interrupt(), execute.join()));
            }
        }
        return Collections.unmodifiableList(lifecycles);
    }
    
    public List<MetaData> getMetaData(ComponentConfiguration configuration) {
        final List<MetaData> metaData;
        Method method = getMethod(configuration);
        if (method == null) {
            metaData = Collections.emptyList();
        } else {
            if (configuration instanceof BeanletConfiguration) {
                Execute execute = ((BeanletConfiguration) configuration).
                        getAnnotationDomain().getDeclaration(Execute.class).
                        getAnnotation(MethodElement.instance(method));
                MetaData m = new ExecuteMetaDataImpl(method,
                        execute == null ? "" : execute.description());
                metaData = Collections.singletonList(m);
            } else {
                metaData = Collections.emptyList();
            }
        }
        return metaData;
    }
    
    private void check(Execute execute, String componentName) {
        assert execute != null;
        if (execute.threads() <= 0) {
            throw new BeanletValidationException(componentName,
                    "Threads MUST be a positive integer: '" +
                    execute.threads() + "'.");
        }
    }
}
