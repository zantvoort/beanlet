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
import org.beanlet.impl.metadata.StartMetaDataImpl;
import org.beanlet.common.AbstractBeanletProvider;
import org.beanlet.common.InvocationImpl;
import org.beanlet.common.event.StartEventImpl;
import org.jargo.MetaData;
import org.jargo.deploy.SequentialDeployable;
import org.beanlet.Start;
import org.beanlet.annotation.MethodElement;
import org.beanlet.plugin.BeanletConfiguration;
import org.jargo.Event;
import org.jargo.ComponentConfiguration;
import org.jargo.EventFactory;
import org.jargo.spi.EventFactoryProvider;
import org.jargo.InvocationFactory;
import org.jargo.spi.InvocationFactoryProvider;
import org.jargo.Invocation;
import org.jargo.ComponentLifecycle;
import org.jargo.ComponentReferenceLifecycle;
import org.jargo.spi.ComponentLifecycleProvider;
import org.jargo.spi.ComponentReferenceLifecycleProvider;
import org.jargo.spi.MetaDataProvider;

/**
 * @author Leon van Zantvoort
 */
public final class StartBeanletProviderImpl extends 
        AbstractBeanletProvider implements InvocationFactoryProvider, 
        EventFactoryProvider, ComponentLifecycleProvider, 
        ComponentReferenceLifecycleProvider, MetaDataProvider {
    
    public Sequence sequence(SequentialDeployable deployable) {
        return Sequence.BEFORE;
    }
    
    public Class<? extends Annotation> annotationType() {
        return Start.class;
    }
    
    public Method getInterfaceMethod() {
        return null;
    }
    
    private Set<Class<? extends Event>> getEventTypes() {
        Set<Class<? extends Event>> types = 
                new HashSet<Class<? extends Event>>();
        types.add(StartEventImpl.class);
        return types;
    }
    
    public List<InvocationFactory> getInvocationFactories(
            ComponentConfiguration<?> configuration) {
        List<InvocationFactory> factories =
                new ArrayList<InvocationFactory>();
        Method method = getMethod(configuration);
        final Set<Method> methods;
        final Invocation invocation;
        if (method != null) {
            methods = Collections.singleton(method);
            invocation = new InvocationImpl(method);
        } else {
            // The Start method is not supported by the underlying object,
            // but that is silently ignored by returning a dummy 
            // invocation factory.
            methods = Collections.emptySet();
            invocation = new InvocationImpl(null) {
                public Object invoke(Object instance) throws Exception {
                    return null;
                }
            };
        }
        Set<Class<? extends Event>> tmp =
                new LinkedHashSet<Class<? extends Event>>();
        tmp.addAll(getEventTypes());
        final Set<Class<? extends Event>> eventTypes = Collections.
                unmodifiableSet(tmp);
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
        return Collections.unmodifiableList(factories);
    }
    
    public List<EventFactory> getEventFactories(
            ComponentConfiguration<?> configuration) {
        Class<? extends Event> cls = StartEventImpl.class;
        Set<Class<? extends Event>> tmp = new HashSet<Class<? extends Event>>();
        tmp.add(cls);
        final Set<Class<? extends Event>> set = tmp;
        EventFactory factory = new EventFactory() {
            public Set<Class<? extends Event>> getEventTypes() {
                return set;
            }
            public Event getEvent(Method method, Object[] args) {
                return null;
            }
            public List<Class<?>> getInterfaces() {
                return Collections.emptyList();
            }
            public boolean isProxy() {
                return false;
            }
        };
        return Collections.singletonList(factory);
    }

    public <T> List<ComponentLifecycle<T>> getComponentLifecycles(
            ComponentConfiguration<T> configuration, Executor executor) {
        List<ComponentLifecycle<T>> lifecycles = 
                new ArrayList<ComponentLifecycle<T>>();
        Method method = getMethod(configuration);
        if (method != null) {
            lifecycles.add(new StartBeanletLifecycleImpl<T>());
        }
        return Collections.unmodifiableList(lifecycles);
    }

    public <T> List<ComponentReferenceLifecycle<T>> getComponentReferenceLifecycles(
            ComponentConfiguration<T> configuration, Executor executor) {
        List<ComponentReferenceLifecycle<T>> lifecycles = 
                new ArrayList<ComponentReferenceLifecycle<T>>();
        Method method = getMethod(configuration);
        if (method != null) {
            lifecycles.add(new StartBeanletLifecycleImpl<T>());
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
                Start start = ((BeanletConfiguration) configuration).
                        getAnnotationDomain().getDeclaration(Start.class).
                        getAnnotation(MethodElement.instance(method));
                assert start != null;
                MetaData m = new StartMetaDataImpl(method, start.
                        description());
                metaData = Collections.singletonList(m);
            } else {
                metaData = Collections.emptyList();
            }
        }
        return metaData;
    }
}
