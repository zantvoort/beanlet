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

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Leon van Zantvoort
 */
public final class ThreadLocalEntityManager extends
        ContainerManagedEntityManager {

    private final BeanletEntityManagerFactory emf;
    private final ThreadLocal<EntityManager> emLocal;
    private final ThreadLocal<AtomicInteger> ixLocal;

    public ThreadLocalEntityManager(BeanletEntityManagerFactory emf) {
        this.emf = emf;
        this.emLocal = new ThreadLocal<EntityManager>();
        this.ixLocal = new ThreadLocal<AtomicInteger>() {
            @Override
            protected AtomicInteger initialValue() {
                return new AtomicInteger(0);
            }
        };
    }

    EntityManager getEntityManager() {
        EntityManager em = emLocal.get();
        if (em == null) {
            throw new PersistenceException("ThreadLocalInvocationInterceptor must be configured.");
        }
        return em;
    }

    @Override
    void preInvoke() {
        if (emLocal.get() == null) {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            emLocal.set(em);
            ixLocal.get().incrementAndGet();
        }
    }

    @Override
    void postInvoke() {
        if (ixLocal.get().decrementAndGet() == 0) {
            EntityManager em = getEntityManager();
            try {
                EntityTransaction tx = em.getTransaction();
                if (tx.getRollbackOnly()) {
                    tx.rollback();
                } else {
                    tx.commit();
                }
            } finally {
                emLocal.remove();
                em.close();
            }
        }
    }
}
