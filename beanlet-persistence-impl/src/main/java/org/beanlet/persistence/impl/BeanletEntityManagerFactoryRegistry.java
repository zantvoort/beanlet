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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceProperty;
import javax.persistence.PersistenceUnit;
import org.jargo.ComponentUnit;

/**
 *
 * @author Leon van Zantvoort
 */
public final class BeanletEntityManagerFactoryRegistry {
    
    // Do not mix container and application factories to prevent clients from
    // closing container factories.
    private static final ConcurrentMap<BeanletPersistenceUnitInfo, 
            BeanletEntityManagerFactory> containerRegistry = new ConcurrentHashMap
            <BeanletPersistenceUnitInfo, BeanletEntityManagerFactory>();
    private static final ConcurrentMap<BeanletPersistenceUnitInfo, 
            BeanletEntityManagerFactory> applicationRegistry = new ConcurrentHashMap
            <BeanletPersistenceUnitInfo, BeanletEntityManagerFactory>();

    private static BeanletEntityManagerFactory createEntityManagerFactory(
            BeanletPersistenceUnitInfo unitInfo, final Map<?, ?> map) {
        EntityManagerFactory emf = BeanletPersistence.
                createContainerEntityManagerFactory(unitInfo, map);
        if (emf == null) {
            throw new PersistenceException("EntityManagerFactory not available " +
                    "for unit '" + unitInfo.getPersistenceUnitName() + "'.");
        }
        return new InternalEntityManagerFactory(emf, map, unitInfo);
    }
    
    public static BeanletEntityManagerFactory getInstance(PersistenceUnit unit,
            ComponentUnit componentUnit) throws PersistenceException {
        BeanletPersistenceUnitInfoFactory factory = 
                BeanletPersistenceUnitInfoFactory.getInstance(componentUnit);
        final BeanletPersistenceUnitInfo unitInfo = factory.getPersistenceUnitInfo(
                unit.unitName());
        if (!applicationRegistry.containsKey(unitInfo)) {
            final BeanletEntityManagerFactory emf = createEntityManagerFactory(unitInfo, Collections.emptyMap());
            BeanletEntityManagerFactory tmp = applicationRegistry.putIfAbsent(unitInfo, emf);
            if (tmp != null) {
                // tmp is also returned on registry.get(unitInfo).
                emf.close();
            } else {
                componentUnit.addDestroyHook(new Runnable() {
                    public void run() {
                        applicationRegistry.remove(unitInfo);
                        emf.close();
                    }
                });
            }
        }
        return applicationRegistry.get(unitInfo);
    }
    
    public static BeanletEntityManagerFactory getInstance(PersistenceContext pctx,
            ComponentUnit componentUnit) throws PersistenceException {
        PersistenceProperty[] props = pctx.properties();
        Map<String, String> map = new HashMap<String, String>();
        for (PersistenceProperty p : props) {
            map.put(p.name(), p.value());
        }
        BeanletPersistenceUnitInfoFactory factory = 
                BeanletPersistenceUnitInfoFactory.getInstance(componentUnit);
        BeanletPersistenceUnitInfo unitInfo = factory.getPersistenceUnitInfo(
                pctx.unitName());
        if (!containerRegistry.containsKey(unitInfo)) {
            final BeanletEntityManagerFactory emf = createEntityManagerFactory(unitInfo, map);
            BeanletEntityManagerFactory tmp = containerRegistry.putIfAbsent(unitInfo, emf);
            if (tmp != null) {
                // tmp is also returned on registry.get(unitInfo).
                emf.close();
            } else {
                componentUnit.addDestroyHook(new Runnable() {
                    public void run() {
                        emf.close();
                    }
                });
            }
        }
        return containerRegistry.get(unitInfo);
    }
}
