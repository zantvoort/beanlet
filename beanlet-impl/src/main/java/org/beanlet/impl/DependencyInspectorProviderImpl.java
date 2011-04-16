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
import org.jargo.DependencyInspector;
import org.jargo.spi.DependencyInspectorProvider;
import org.jargo.ComponentConfiguration;
import org.jargo.ComponentRegistration;
import org.jargo.deploy.Deployable;
import org.jargo.deploy.Deployer;
import org.jargo.spi.Provider;

/**
 * @author Leon van Zantvoort
 */
public final class DependencyInspectorProviderImpl extends AbstractProvider 
        implements DependencyInspectorProvider, Deployer {
    
    private final List<DependencyInjectionFactoryProvider> providers;
    private final AtomicReference<List<DependencyInjectionFactoryProvider>> ref;
    private final ConcurrentMap<ComponentConfiguration, List<DependencyInspector>> inspectorMap;

    public DependencyInspectorProviderImpl() {
        providers = new CopyOnWriteArrayList<DependencyInjectionFactoryProvider>();
        ref = new AtomicReference<List<DependencyInjectionFactoryProvider>>();
        ref.set(providers);
        inspectorMap = new ConcurrentHashMap<ComponentConfiguration, List<DependencyInspector>>();
    }
    
    private List<DependencyInspector> getDependencyInspectors(
            BeanletConfiguration configuration) {
        List<DependencyInspector> inspectors = inspectorMap.get(configuration);
        if (inspectors == null) {
            List<DependencyInjectionFactory> list = 
                    new ArrayList<DependencyInjectionFactory>();
            for (DependencyInjectionFactoryProvider provider : ref.get()) {
                list.addAll(provider.getDependencyInjectionFactories(configuration));
            }
            DependencyInspector inspector = new DependencyInspectorImpl(configuration,
                    new DependencyInjectionFactories(configuration, list));
            inspectors = Collections.singletonList(inspector);
            inspectorMap.put(configuration, inspectors);
        }
        return inspectors;
    }
    
    public List<DependencyInspector> getDependencyInspectors(
            final ComponentConfiguration configuration) {
        final List<DependencyInspector> list;
        if (configuration instanceof BeanletConfiguration) {
            list = getDependencyInspectors((BeanletConfiguration) configuration);
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
            inspectorMap.clear();
            providers.add((DependencyInjectionFactoryProvider) deployable);
            ref.set(sort(providers));
        }
    }

    public void undeploy(Deployable deployable) throws Exception {
        if (deployable instanceof DependencyInjectionFactoryProvider) {
            inspectorMap.clear();
            providers.remove((DependencyInjectionFactoryProvider) deployable);
            ref.set(sort(providers));
        }
        if (deployable instanceof ComponentRegistration) {
            List<ComponentConfiguration<?>> configurations = 
                    ((ComponentRegistration) deployable).getComponentConfigurations();
            inspectorMap.keySet().removeAll(configurations);
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
