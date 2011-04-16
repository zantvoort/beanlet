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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import org.beanlet.common.AbstractProvider;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.DependencyInjectionFactory;
import org.beanlet.plugin.spi.DependencyInjectionFactoryProvider;
import org.jargo.ComponentConfiguration;
import org.jargo.ComponentRegistration;
import org.jargo.InjectionFactory;
import org.jargo.deploy.Deployable;
import org.jargo.deploy.Deployer;
import org.jargo.spi.InjectionFactoryProvider;
import org.jargo.spi.Provider;

/**
 * @author Leon van Zantvoort
 */
public final class InjectionFactoryProviderImpl extends AbstractProvider
        implements InjectionFactoryProvider, Deployer {

    private final List<DependencyInjectionFactoryProvider> providers;
    private final AtomicReference<List<DependencyInjectionFactoryProvider>> ref;
    private final ConcurrentMap<ComponentConfiguration<?>, List<InjectionFactory<?>>> factoryMap;
    
    public InjectionFactoryProviderImpl() {
        ref = new AtomicReference<List<DependencyInjectionFactoryProvider>>();
        providers = new CopyOnWriteArrayList<DependencyInjectionFactoryProvider>();
        ref.set(providers);
        factoryMap = new ConcurrentHashMap<ComponentConfiguration<?>, List<InjectionFactory<?>>>();
    }
    
    private <T> List<InjectionFactory<T>> getInjectionFactories(
            BeanletConfiguration<T> configuration) {
        List<InjectionFactory<?>> factories = factoryMap.get(configuration);
        if (factories == null) {
            List<DependencyInjectionFactory> list = 
                    new ArrayList<DependencyInjectionFactory>();
            for (DependencyInjectionFactoryProvider provider : ref.get()) {
                list.addAll(provider.getDependencyInjectionFactories(configuration));
            }
            InjectionFactory<T> factory = new InjectionFactoryImpl<T>(
                    configuration, new DependencyInjectionFactories(
                    configuration, list));
            factories = Collections.<InjectionFactory<?>>singletonList(factory);
            factoryMap.put(configuration, factories);
        }
        @SuppressWarnings("unchecked")
        List<InjectionFactory<T>> tmp = (List<InjectionFactory<T>>) 
                (List<?>) factories;
        return tmp;
    }
    
    public <T> List<InjectionFactory<T>> getInjectionFactories(
            final ComponentConfiguration<T> configuration) {
        final List<InjectionFactory<T>> list;
        if (configuration instanceof BeanletConfiguration<?>) {
            list = getInjectionFactories((BeanletConfiguration<T>) configuration);
        } else {
            list = Collections.emptyList();
        }
        return list;
    }
    
    public void setParent(Deployer deployer) {
        // Do nothing.
    }
    
    public void deploy(Deployable deployable) throws Exception {
        if (deployable instanceof DependencyInjectionFactoryProvider) {
            factoryMap.clear();
            providers.add((DependencyInjectionFactoryProvider) deployable);
            ref.set(sort(providers));
        }
    }
    
    public void undeploy(Deployable deployable) throws Exception {
        if (deployable instanceof DependencyInjectionFactoryProvider) {
            factoryMap.clear();
            providers.remove((DependencyInjectionFactoryProvider) deployable);
            ref.set(sort(providers));
        }
        if (deployable instanceof ComponentRegistration) {
            List<ComponentConfiguration<?>> configurations = 
                    ((ComponentRegistration) deployable).getComponentConfigurations();
            factoryMap.keySet().removeAll(configurations);
        }
    }
    
    /**
     * Sorts the specified {@code providers} list according to the rules of
     * the {@code SequentialDeployable} interface.
     */
    private <T extends Provider> List<T> sort(List<T> providers) {
        List<T> list = new ArrayList<T>(providers);
        Collections.sort(list, new SequentialDeployableComparator<T>());
        return list;
    }
}
