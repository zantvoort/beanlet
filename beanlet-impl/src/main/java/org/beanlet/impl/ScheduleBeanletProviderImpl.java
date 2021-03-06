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
import org.beanlet.impl.metadata.ScheduleMetaDataImpl;
import org.beanlet.common.AbstractBeanletProvider;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.common.Beanlets;
import org.beanlet.common.InvocationImpl;
import org.beanlet.common.event.ScheduleEventImpl;
import org.jargo.Event;
import org.jargo.ComponentReferenceLifecycle;
import org.beanlet.Schedule;
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
public final class ScheduleBeanletProviderImpl extends
        AbstractBeanletProvider implements InvocationFactoryProvider,
        EventFactoryProvider, ComponentLifecycleProvider,
        ComponentReferenceLifecycleProvider, MetaDataProvider {
    
    public Class<? extends Annotation> annotationType() {
        return Schedule.class;
    }
    
    public Method getInterfaceMethod() {
        return null;
    }
    
    private Set<Class<? extends Event>> getEventTypes() {
        Set<Class<? extends Event>> types =
                new HashSet<Class<? extends Event>>();
        types.add(ScheduleEventImpl.class);
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
            factories.add(new ScheduleEventFactoryImpl(configuration));
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
                Schedule schedule = domain.getDeclaration(Schedule.class).
                        getAnnotation(MethodElement.instance(method));
                check(schedule, configuration.getComponentName());
                CronExpression cron = schedule.cron().equals("") ? null :
                    new CronExpression(schedule.cron());
                lifecycles.add(new ScheduleBeanletLifecycleImpl<T>(
                        executor, schedule.once(), schedule.initialDelay(), 
                        schedule.delay(), schedule.rate(), cron,
                        schedule.fireAll(), schedule.interrupt(), 
                        schedule.join()));
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
                Schedule schedule = domain.getDeclaration(Schedule.class).
                        getAnnotation(MethodElement.instance(method));
                check(schedule, configuration.getComponentName());
                CronExpression cron = schedule.cron().equals("") ? null :
                    new CronExpression(schedule.cron());
                lifecycles.add(new ScheduleBeanletLifecycleImpl<T>(
                        executor, schedule.once(), schedule.initialDelay(), 
                        schedule.delay(), schedule.rate(), cron,
                        schedule.fireAll(), schedule.interrupt(), 
                        schedule.join()));
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
                Schedule schedule = ((BeanletConfiguration) configuration).
                        getAnnotationDomain().getDeclaration(Schedule.class).
                        getAnnotation(MethodElement.instance(method));
                MetaData m = new ScheduleMetaDataImpl(method,
                        schedule == null ? "" : schedule.description());
                metaData = Collections.singletonList(m);
            } else {
                metaData = Collections.emptyList();
            }
        }
        return metaData;
    }
    
    private void check(Schedule schedule, String componentName) {
        assert schedule != null;
        if (schedule.initialDelay() < 0) {
            throw new BeanletValidationException(componentName,
                    "Initial delay MUST be greater than 0: '" +
                    schedule.delay() + "'.");
        }
        if (schedule.delay() < 0) {
            throw new BeanletValidationException(componentName,
                    "Delay MUST be greater than 0: '" +
                    schedule.delay() + "'.");
        }
        if (schedule.rate() < 0) {
            throw new BeanletValidationException(componentName,
                    "Rate MUST be greater than 0: '" +
                    schedule.rate() + "'.");
        }
        if (schedule.delay() > 0 && schedule.fireAll()) {
            throw new BeanletValidationException(componentName,
                    "Delay and fireAll must not be mixed.");
        }
        
        int counter = 0;
        if (schedule.delay() > 0) {
            counter ++;
        }
        if (schedule.rate() > 0) {
            counter ++;
        }
        if (!schedule.cron().equals("")) {
            counter++;
        }
        if (counter > 0 && schedule.once()) {
            throw new BeanletValidationException(componentName,
                    "Delay, rate and cron NOT supported while scheduled once.");
        }
        if (counter > 1) {
            throw new BeanletValidationException(componentName,
                    "Delay, rate and cron MUST NOT both be mixed.");
        }
    }
}
