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
import java.util.IdentityHashMap;
import java.util.Map;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

import org.beanlet.common.BeanletStack;

/**
 * This class is used internally and should never be passed to the client 
 * application. This class has package private visibility.
 *
 * @author Leon van Zantvoort
 */
final class InternalNonJTAEntityManagerFactory implements EntityManagerFactory {
    
    private final EntityManagerFactory emf;
    private final ThreadLocal<BeanletStack<Map<Object, EntityManager>>> localStack;
    
    public InternalNonJTAEntityManagerFactory(EntityManagerFactory emf) {
        this.emf = emf;
        this.localStack = new ThreadLocal<BeanletStack<Map<Object, EntityManager>>>() {
            protected BeanletStack<Map<Object, EntityManager>> initialValue() {
                return new BeanletStack<Map<Object, EntityManager>>();
            }
        };
    }
    
    public void preInvoke() {
        localStack.get().push(new IdentityHashMap<Object, EntityManager>());
    }
    
    public void postInvoke() {
        Map<Object, EntityManager> map = localStack.get().pop();
        for (EntityManager em : map.values()) {
            try {
                em.close();
            } catch (Exception e) {
                // Ignore.
            }
        }
    }

    public EntityManager createEntityManager() {
        return createEntityManager(Collections.emptyMap());
    }

    public EntityManager createEntityManager(Map map) {
        BeanletStack<Map<Object, EntityManager>> stack = localStack.get();
        Map<Object, EntityManager> stacked = stack.peek();
        assert stacked != null;
        EntityManager em = stacked.get(this);
        if (em == null) {
            em = emf.createEntityManager(map);
            stacked.put(this, em);
        }
        return em;
    }
    
    public boolean isOpen() {
        return emf.isOpen();
    }

    public void close() {
        emf.close();
    }

    public Cache getCache() {
        return emf.getCache();
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return emf.getCriteriaBuilder();
    }

    public Metamodel getMetamodel() {
        return emf.getMetamodel();
    }

    public Map<String, Object> getProperties() {
        return emf.getProperties();
    }

    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return emf.getPersistenceUnitUtil();
    }
}
