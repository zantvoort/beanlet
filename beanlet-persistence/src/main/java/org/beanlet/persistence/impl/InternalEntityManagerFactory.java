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

import java.util.Map;
import javax.persistence.Cache;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

/**
 * This class is used internally and should never be passed to the client 
 * application. This class has package private visibility.
 */
class InternalEntityManagerFactory implements BeanletEntityManagerFactory {

    private final EntityManagerFactory emf;
    private final Map<?, ?> map;
    private final BeanletPersistenceUnitInfo unitInfo;

    public InternalEntityManagerFactory(EntityManagerFactory emf, Map<?, ?> map, BeanletPersistenceUnitInfo unitInfo) {
        this.emf = emf;
        this.map = map;
        this.unitInfo = unitInfo;
    }

    public BeanletEntityManager createEntityManager() {
        return new InternalEntityManager(emf.createEntityManager(map));
    }

    public BeanletEntityManager createEntityManager(Map map) {
        return new InternalEntityManager(emf.createEntityManager(map));
    }

    public boolean isOpen() {
        return emf.isOpen();
    }

    public void close() {
        emf.close();
    }

    public BeanletPersistenceUnitInfo getPersistenceUnitInfo() {
        return unitInfo;
    }

    public Cache getCache() {
        return emf.getCache();
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Metamodel getMetamodel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, Object> getProperties() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}