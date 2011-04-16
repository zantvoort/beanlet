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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import org.beanlet.Event;
import org.beanlet.plugin.BeanletEventFactory;
import org.beanlet.plugin.spi.BeanletEventFactoryProvider;
import org.jargo.deploy.Deployable;
import org.jargo.deploy.Deployer;
import org.jargo.spi.Provider;

/**
 *
 * @author Leon van Zantvoort
 */
public final class BeanletEventFactories implements BeanletEventFactory, 
        Deployer {

    private static final ConcurrentMap<Class<? extends Event>, BeanletEventFactory> cache = 
            new ConcurrentHashMap<Class<? extends Event>, BeanletEventFactory>();
    private static final CopyOnWriteArraySet<BeanletEventFactoryProvider> providers = 
            new CopyOnWriteArraySet<BeanletEventFactoryProvider>();
    private static final AtomicReference<List<BeanletEventFactory>> factories =
            new AtomicReference<List<BeanletEventFactory>>();
    private static final BeanletEventFactory factory = new BeanletEventFactories();
    
    static {
        factories.set(new ArrayList<BeanletEventFactory>());
    }
    
    public static BeanletEventFactory getInstance() {
        return factory;
    }
    
    public void setParent(Deployer deployer) {
    }
    
    public void deploy(Deployable deployable) throws Exception {
        if (deployable instanceof BeanletEventFactoryProvider) {
            if (providers.add((BeanletEventFactoryProvider) deployable)) {
                List<BeanletEventFactory> list = 
                        new ArrayList<BeanletEventFactory>();
                for (BeanletEventFactoryProvider provider : getSortedList(
                        new ArrayList<BeanletEventFactoryProvider>(providers))) {
                    list.addAll(provider.getBeanletEventFactories());
                }
                cache.clear();
                factories.set(list);
            }
        }
    }
    
    public void undeploy(Deployable deployable) throws Exception {
        if (deployable instanceof BeanletEventFactoryProvider) {
            if (providers.remove((BeanletEventFactoryProvider) deployable)) {
                List<BeanletEventFactory> list = 
                        new ArrayList<BeanletEventFactory>();
                for (BeanletEventFactoryProvider provider : getSortedList(
                        new ArrayList<BeanletEventFactoryProvider>(providers))) {
                    list.addAll(provider.getBeanletEventFactories());
                }
                cache.clear();
                factories.set(list);
            }
        }
    }
    
    public <T extends Event> T getEvent(Class<T> eventClass) {
        BeanletEventFactory factory = cache.get(eventClass);
        if (factory != null) {
            T event = factory.getEvent(eventClass);
            assert event != null;
            return event;
        }
        
        Event event = null;
        for (BeanletEventFactory f : factories.get()) {
            event = f.getEvent(eventClass);
            if (event != null) {
                cache.put(eventClass, f);
                break;
            }
        }
        
        @SuppressWarnings("unchecked")
        T t = (T) event;
        return t;
    }

    /**
     * Sorts the specified {@code providers} list according to the rules of
     * the {@code SequentialDeployable} interface.
     */
    private <T extends Provider> List<T> getSortedList(
            List<T> providers) {
        List<T> newList =
                new ArrayList<T>(providers);
        Collections.sort(newList,
                new SequentialDeployableComparator<T>());
        return newList;
    }
}
