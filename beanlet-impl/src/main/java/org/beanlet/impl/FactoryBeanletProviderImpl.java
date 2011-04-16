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
import org.beanlet.impl.metadata.FactoryMetaDataImpl;
import org.beanlet.common.AbstractBeanletProvider;
import org.beanlet.common.event.FactoryEventImpl;
import org.beanlet.common.InvocationImpl;
import org.jargo.Event;
import org.jargo.ComponentConfiguration;
import org.jargo.MetaData;
import org.beanlet.Factory;
import org.beanlet.FactoryBeanlet;
import org.jargo.EventFactory;
import org.beanlet.annotation.MethodElement;
import org.beanlet.plugin.BeanletConfiguration;
import org.jargo.spi.EventFactoryProvider;
import org.jargo.InvocationFactory;
import org.jargo.spi.InvocationFactoryProvider;
import org.jargo.Invocation;
import org.jargo.spi.MetaDataProvider;

/**
 * @author Leon van Zantvoort
 */
public final class FactoryBeanletProviderImpl extends 
        AbstractBeanletProvider implements InvocationFactoryProvider, 
        EventFactoryProvider, MetaDataProvider {
    
    public Class<? extends Annotation> annotationType() {
        return Factory.class;
    }
    
    public Method getInterfaceMethod() {
        try {
            return FactoryBeanlet.class.getMethod("getObject");
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }
    
    private Set<Class<? extends Event>> getEventTypes() {
        Set<Class<? extends Event>> types = 
                new HashSet<Class<? extends Event>>();
        types.add(FactoryEventImpl.class);
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
    
    public List<EventFactory> getEventFactories(ComponentConfiguration configuration) {
        List<EventFactory> factories = new ArrayList<EventFactory>();
        if (getMethod(configuration) != null) {
            factories.add(new FactoryEventFactoryImpl(configuration));
        }
        return Collections.unmodifiableList(factories);
    }

    public List<MetaData> getMetaData(ComponentConfiguration configuration) {
        final List<MetaData> metaData;
        Method method = getMethod(configuration);
        if (method == null) {
            metaData = Collections.emptyList();
        } else {
            if (configuration instanceof BeanletConfiguration) {
                Factory factory = ((BeanletConfiguration) configuration).
                        getAnnotationDomain().getDeclaration(Factory.class).
                        getAnnotation(MethodElement.instance(method));
                MetaData m = new FactoryMetaDataImpl(method, 
                        factory == null ? "" : factory.description());
                metaData = Collections.singletonList(m);
            } else {
                metaData = Collections.emptyList();
            }
        }
        return metaData;
    }
}
