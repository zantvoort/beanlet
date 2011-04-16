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
package org.beanlet.persistence.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import org.beanlet.persistence.PersistencePropertiesFactory;
import org.beanlet.persistence.spi.PersistencePropertiesFactoryProvider;
import org.jargo.deploy.Deployable;
import org.jargo.deploy.Deployer;
import org.jargo.spi.Provider;

/**
 *
 * @author Leon van Zantvoort
 */
public final class PersistencePropertyFactories implements 
        PersistencePropertiesFactory, Deployer {

    private static final CopyOnWriteArraySet<PersistencePropertiesFactoryProvider> providers = 
            new CopyOnWriteArraySet<PersistencePropertiesFactoryProvider>();
    private static final AtomicReference<Map> properties =
            new AtomicReference<Map>();
    private static final PersistencePropertiesFactory factory = new PersistencePropertyFactories();
    
    static {
        properties.set(new HashMap());
    }
    
    public static PersistencePropertiesFactory getInstance() {
        return factory;
    }
    
    public void setParent(Deployer deployer) {
    }
    
    public void deploy(Deployable deployable) throws Exception {
        if (deployable instanceof PersistencePropertiesFactoryProvider) {
            if (providers.add((PersistencePropertiesFactoryProvider) deployable)) {
                Map<Object, Object> map = new HashMap<Object, Object>();
                for (PersistencePropertiesFactoryProvider provider : getSortedList(
                        new ArrayList<PersistencePropertiesFactoryProvider>(providers))) {
                    for (PersistencePropertiesFactory tmp : provider.getPersistencePropertiesFactories()) {
                        map.putAll(tmp.getProperties());
                    }
                }
                properties.set(Collections.unmodifiableMap(map));
            }
        }
    }
    
    public void undeploy(Deployable deployable) throws Exception {
        if (deployable instanceof PersistencePropertiesFactoryProvider) {
            if (providers.remove((PersistencePropertiesFactoryProvider) deployable)) {
                Map<Object, Object> map = new HashMap<Object, Object>();
                for (PersistencePropertiesFactoryProvider provider : getSortedList(
                        new ArrayList<PersistencePropertiesFactoryProvider>(providers))) {
                    for (PersistencePropertiesFactory tmp : provider.getPersistencePropertiesFactories()) {
                        map.putAll(tmp.getProperties());
                    }
                }
                properties.set(Collections.unmodifiableMap(map));
            }
        }
    }
    
    public Map<?, ?> getProperties() {
        return properties.get();
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
