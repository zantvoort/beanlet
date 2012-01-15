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

import java.util.ArrayList;
import static org.beanlet.common.Beanlets.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.beanlet.BeanletApplicationContext;
import org.beanlet.BeanletApplicationException;
import org.beanlet.BeanletCreationException;
import org.beanlet.BeanletFactory;
import org.beanlet.BeanletNotFoundException;
import org.beanlet.BeanletNotOfRequiredTypeException;
import org.beanlet.BeanletReference;
import org.beanlet.Event;
import org.beanlet.FactoryBeanlet;
import org.beanlet.event.FactoryEvent;
import org.beanlet.metadata.FactoryMetaData;
import org.beanlet.plugin.BeanletEventFactory;
import org.jargo.ComponentApplicationContext;
import org.jargo.ComponentFactory;
import org.jargo.ComponentNotFoundException;

/**
 *
 * @author Leon van Zantvoort
 */
public final class BeanletApplicationContextImpl extends 
        BeanletApplicationContext {

    private final BeanletEventFactory factory;
    
    public BeanletApplicationContextImpl() {
        this.factory = BeanletEventFactories.getInstance();
    }

    protected BeanletApplicationContext resolveInstance() throws 
            BeanletApplicationException {
        ComponentApplicationContext.instance();
        return this;
    }
    
    public void shutdown() {
        ComponentApplicationContext.instance().shutdown();
    }
    
    public <T extends Event> T getEvent(Class<T> eventClass) {
        return factory.getEvent(eventClass);
    }

    public Object getBeanlet(String beanletName) throws
            BeanletNotFoundException, BeanletCreationException {
        return getBeanlet(beanletName, (Class<?>) null);
    }
    
    public <T> T getBeanlet(String beanletName, Class<T> requiredType) throws 
            BeanletNotFoundException, BeanletCreationException, 
            BeanletNotOfRequiredTypeException {
        return getBeanlet(beanletName, requiredType, null);
    }
    
    public Object getBeanlet(String beanletName, Map<String, ?> info) throws 
            BeanletNotFoundException, BeanletCreationException {
        return getBeanlet(beanletName, null, info);
    }
    
    public <T> T getBeanlet(String beanletName, Class<T> requiredType,
            Map<String, ?> info) throws BeanletNotFoundException, 
            BeanletCreationException, BeanletNotOfRequiredTypeException {
        final Object beanlet;
        final boolean prefix;
        if (beanletName.startsWith(FACTORY_BEANLET_PREFIX)) {
            beanletName = beanletName.substring(1);
            prefix = true;
        } else {
            prefix = false;
        }
        BeanletReference<?> reference = getBeanletFactory(beanletName).create(info);
        if (prefix) {
            beanlet = reference.getBeanlet();
        } else {
            Object tmp = null;
            FactoryEvent event = getEvent(FactoryEvent.class);
            if (reference.isExecutable(event)) {
                tmp = reference.execute(event);
            } else {
                tmp = reference.getBeanlet();
            }
            while (tmp instanceof FactoryBeanlet) {
                Object t = ((FactoryBeanlet) tmp).getObject();
                if (tmp == t) {
                    // Prevent infinite loop.
                    // PENDING: throw exception instead?
                    break;
                }
                tmp = t;
            }
            beanlet = tmp;
        }

        if (beanlet != null && requiredType != null && !requiredType.
                isAssignableFrom(beanlet.getClass())) {
            throw new BeanletNotOfRequiredTypeException(beanletName, 
                    requiredType, beanlet.getClass());
        }
        @SuppressWarnings("unchecked")
        T t = (T) beanlet;
        return t;
    }
    
    public Set<String> getBeanletNames() {
        Set<String> names = new HashSet<String>();
        for (ComponentFactory<?> factory : 
                ComponentApplicationContext.instance().getComponentFactories()) {
            names.add(factory.getComponentMetaData().getComponentName());
        }
        return Collections.unmodifiableSet(names);
    }

    public Set<String> getBeanletNamesForType(Class<?> type) {
        Set<String> names = new HashSet<String>();
        for (ComponentFactory<?> factory : ComponentApplicationContext.
                instance().getComponentFactoriesForType(type)) {
            names.add(factory.getComponentMetaData().getComponentName());
        }
        return Collections.unmodifiableSet(names);
    }
    
    public Set<String> getBeanletNamesForType(Class<?> type, 
            boolean factoryAware, boolean usePrefix) {
        if (!factoryAware) {
            return getBeanletNamesForType(type);
        }
        Set<String> names = new HashSet<String>();
        for (ComponentFactory<?> f : ComponentApplicationContext.instance().
                getComponentFactories()) {
            BeanletFactory<?> factory = BeanletFactoryImpl.instance(f);
            if (isFactoryBeanlet(factory)) {
                if (isTypeMatch(factory, type)) {
                    if (usePrefix) {
                        names.add(FACTORY_BEANLET_PREFIX + 
                                factory.getBeanletMetaData().getBeanletName());
                    } else {
                        names.add(factory.getBeanletMetaData().getBeanletName());
                    }
                }
                if (isFactoryTypeMatch(factory, type)) {
                    names.add(factory.getBeanletMetaData().getBeanletName());
                }
            } else {
                if (isTypeMatch(factory, type)) {
                    names.add(factory.getBeanletMetaData().getBeanletName());
                }
            }
        }
        return Collections.unmodifiableSet(names);
    }

    private boolean isFactoryBeanlet(BeanletFactory<?> factory) {
        return factory.getBeanletMetaData().isMetaDataPresent(
                FactoryMetaData.class);
    }
    
    private boolean isFactoryTypeMatch(BeanletFactory<?> factory, Class<?> type) {
        List<FactoryMetaData> m = factory.getBeanletMetaData().getMetaData(
                FactoryMetaData.class);
        assert m.size() == 1;
        Class<?> returnType = m.get(0).getReturnType();
        return !FactoryBeanlet.class.isAssignableFrom(returnType) && 
                type.isAssignableFrom(returnType);
    }
    
    private boolean isTypeMatch(BeanletFactory<?> factory, Class<?> type) {
        final List<Class<?>> types;
        if (factory.getBeanletMetaData().isVanilla()) {
            types = new ArrayList<Class<?>>(factory.getBeanletMetaData().getInterfaces());
            types.add(factory.getBeanletMetaData().getType());
        } else {
            types = factory.getBeanletMetaData().getInterfaces();
        }
        for (Class<?> t : types) {
            if (type.isAssignableFrom(t)) {
                return true;
            }
        }
        return false;
    }
    
    public BeanletFactory<?> getBeanletFactory(String beanletName) throws 
            BeanletNotFoundException {
        try {
            if (beanletName.startsWith(FACTORY_BEANLET_PREFIX)) {
                beanletName = beanletName.substring(1);
            }
            return BeanletFactoryImpl.instance(
                    ComponentApplicationContext.instance().
                    getComponentFactory(beanletName));
        } catch (ComponentNotFoundException e) {
            throw new BeanletNotFoundException(e.getComponentName(),
                    CHAIN_JARGO_EXCEPTIONS ? e : e.getCause());
        }
    }

    public <T> BeanletFactory<? extends T> getBeanletFactory(String beanletName,
            Class<T> requiredType) throws BeanletNotFoundException {
        BeanletFactory<?> tmp = getBeanletFactory(beanletName);
        if (!isTypeMatch(tmp, requiredType)) {
            throw new BeanletNotOfRequiredTypeException(beanletName, 
                    requiredType, tmp.getBeanletMetaData().getType());
        }
        @SuppressWarnings("unchecked")
        BeanletFactory<? extends T> factory = (BeanletFactory<? extends T>) tmp;
        return factory;
    }
    
    public boolean exists(String beanletName) {
        if (beanletName.startsWith(FACTORY_BEANLET_PREFIX)) {
            beanletName = beanletName.substring(1);
        }
        return ComponentApplicationContext.instance().exists(beanletName);
    }    
}
