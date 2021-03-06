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
import java.util.Map;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import org.beanlet.plugin.TransactionLocal;

/**
 * This class is used internally and should never be passed to the client 
 * application. This class has package private visibility.
 *
 * @author Leon van Zantvoort
 */
final class InternalJTAEntityManagerFactory implements EntityManagerFactory {    
    
    private final EntityManagerFactory emf;
    private final TransactionLocal<EntityManager> txLocal;
    
    public InternalJTAEntityManagerFactory(EntityManagerFactory emf) {
        this.emf = emf;
        this.txLocal = new TransactionLocal<EntityManager>();
    }
    
    /**
     * Returns {@code true} if running thread is participating in an active
     * transaction.
     *
     * @throws PersistenceException if failed to obtain transaction status.
     */
    public boolean isTransactionActive() throws PersistenceException {
        try {
            Transaction tx = txLocal.getTransaction();
            return tx != null && tx.getStatus() == Status.STATUS_ACTIVE;
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }
    
    void preInvoke() {
        if (isTransactionActive()) {
            EntityManager tmp = txLocal.get();
            if (tmp == null) {
                try {
                    final EntityManager em = emf.createEntityManager();
                    Transaction tx = txLocal.getTransaction();
                    tx.registerSynchronization(new Synchronization() {
                        public void beforeCompletion() {
                        }
                        public void afterCompletion(int i) {
                            em.close();
                        }
                    });
                    em.joinTransaction();
                    txLocal.set(em);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new PersistenceException(e);
                }
            }
        }
    }
    
    void postInvoke() {
        // Do nothing.
    }
    
    public EntityManager createEntityManager() {
        return createEntityManager(Collections.emptyMap());
    }

    public EntityManager createEntityManager(Map map) {
        // prepareInvoke must be called within the scope of the running transaction.
        EntityManager em = txLocal.get();
        assert em != null;
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
