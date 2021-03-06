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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.beanlet.plugin.DependencyInjection;
import org.beanlet.plugin.DependencyInjectionFactory;

/**
 * This dependency injection factory takes advantage of the fact that its 
 * underlying factories can return static information.
 *
 * @author Leon van Zantvoort
 */
public final class CachingDependencyInjectionFactory implements 
        DependencyInjectionFactory {
    
    private final DependencyInjectionFactory factory;
    private final ConcurrentMap<Class, List<DependencyInjection>> constructorCache;
    private final ConcurrentMap<Class, List<DependencyInjection>> setterCache;
    private final ConcurrentMap<Object, List<DependencyInjection>> factoryCache;
    
    public CachingDependencyInjectionFactory(
            DependencyInjectionFactory factory) {
        this.factory = factory;
        this.constructorCache = 
                new ConcurrentHashMap<Class, List<DependencyInjection>>();
        this.setterCache = 
                new ConcurrentHashMap<Class, List<DependencyInjection>>();
        this.factoryCache = 
                new ConcurrentHashMap<Object, List<DependencyInjection>>();
    }

    public List<DependencyInjection> getConstructorDependencyInjections(
            Class<?> cls) {
        List<DependencyInjection> list = constructorCache.get(cls);
        if (list == null) {
            list = new ArrayList<DependencyInjection>();
            for (DependencyInjection injection : 
                    factory.getConstructorDependencyInjections(cls)) {
                list.add(new CachingDependencyInjection(injection));
            }
            list = Collections.unmodifiableList(list);
            constructorCache.putIfAbsent(cls, list);
        }
        return constructorCache.get(cls);
    }

    public List<DependencyInjection> getSetterDependencyInjections(
            Class<?> cls) {
        List<DependencyInjection> list = setterCache.get(cls);
        if (list == null) {
            list = new ArrayList<DependencyInjection>();
            for (DependencyInjection injection : 
                    factory.getSetterDependencyInjections(cls)) {
                list.add(new CachingDependencyInjection(injection));
            }
            list = Collections.unmodifiableList(list);
            setterCache.putIfAbsent(cls, list);
        }
        return setterCache.get(cls);
    }
    
    public List<DependencyInjection> getFactoryDependencyInjections(
            Class<?> cls, String factoryMethod) {
        Object key = Arrays.asList(cls, factoryMethod);
        List<DependencyInjection> list = factoryCache.get(key);
        if (list == null) {
            list = new ArrayList<DependencyInjection>();
            for (DependencyInjection injection : 
                    factory.getFactoryDependencyInjections(cls, factoryMethod)) {
                list.add(new CachingDependencyInjection(injection));
            }
            list = Collections.unmodifiableList(list);
            factoryCache.putIfAbsent(key, list);
        }
        return factoryCache.get(key);
    }
}
