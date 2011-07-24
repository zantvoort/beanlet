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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
    private static final Map<BeanletPersistenceUnitInfo,
            BeanletEntityManagerFactory> containerRegistry = new HashMap
            <BeanletPersistenceUnitInfo, BeanletEntityManagerFactory>();
    private static final Map<BeanletPersistenceUnitInfo,
            BeanletEntityManagerFactory> applicationRegistry = new HashMap
            <BeanletPersistenceUnitInfo, BeanletEntityManagerFactory>();

    // Locking introduced, as 3CPO can run into deadlocks in the previous optimistic approach.
    private static final Lock lock = new ReentrantLock();
    private static final Map<BeanletPersistenceUnitInfo, ReadWriteLock> containerLockMap = new HashMap<BeanletPersistenceUnitInfo, ReadWriteLock>();
    private static final Map<BeanletPersistenceUnitInfo, ReadWriteLock> applicationLockMap = new HashMap<BeanletPersistenceUnitInfo, ReadWriteLock>();

    private static BeanletEntityManagerFactory createEntityManagerFactory(
            BeanletPersistenceUnitInfo unitInfo, final Map<?, ?> map) {
        EntityManagerFactory emf = PersistenceHelper.
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
        lock.lock();
        ReadWriteLock rwLock;
        try {
            rwLock = applicationLockMap.get(unitInfo);
            if (rwLock == null) {
                rwLock = new ReentrantReadWriteLock();
                applicationLockMap.put(unitInfo, rwLock);
            }
        } finally {
            lock.unlock();
        }

        rwLock.readLock().lock();
        BeanletEntityManagerFactory emf = applicationRegistry.get(unitInfo);
        rwLock.readLock().unlock();
        if (emf == null) {
            try {
                rwLock.writeLock().lockInterruptibly();
                emf = applicationRegistry.get(unitInfo);
                if (emf == null) {
                    emf = createEntityManagerFactory(unitInfo, Collections.emptyMap());
                    applicationRegistry.put(unitInfo, emf);
                    componentUnit.addDestroyHook(new Runnable() {
                        public void run() {
                            applicationLockMap.remove(unitInfo);
                            applicationRegistry.remove(unitInfo).close();
                        }
                    });
                }
            } catch (InterruptedException e) {
                throw new PersistenceException(e);
            } finally {
                rwLock.writeLock().unlock();
            }
        }
        assert emf != null;
        return emf;
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
        final BeanletPersistenceUnitInfo unitInfo = factory.getPersistenceUnitInfo(
                pctx.unitName());
        lock.lock();
        ReadWriteLock rwLock;
        try {
            rwLock = containerLockMap.get(unitInfo);
            if (rwLock == null) {
                rwLock = new ReentrantReadWriteLock();
                containerLockMap.put(unitInfo, rwLock);
            }
        } finally {
            lock.unlock();
        }
        rwLock.readLock().lock();
        BeanletEntityManagerFactory emf = containerRegistry.get(unitInfo);
        rwLock.readLock().unlock();
        if (emf == null) {
            try {
                rwLock.writeLock().lockInterruptibly();
                emf = containerRegistry.get(unitInfo);
                if (emf == null) {
                    emf = createEntityManagerFactory(unitInfo, map);
                    containerRegistry.put(unitInfo, emf);
                    componentUnit.addDestroyHook(new Runnable() {
                        public void run() {
                            containerLockMap.remove(unitInfo);
                            containerRegistry.remove(unitInfo).close();
                        }
                    });
                }
            } catch (InterruptedException e) {
                throw new PersistenceException(e);
            } finally {
                rwLock.writeLock().unlock();
            }
        }
        assert emf != null;
        return emf;
    }
}
