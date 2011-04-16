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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceUnitTransactionType;
import org.beanlet.plugin.TransactionLocal;
import org.jargo.ComponentUnit;

/**
 * Provides access to the persistence context.
 *
 * @author Leon van Zantvoort
 */
public final class BeanletPersistenceContext {
    
    private final static TransactionLocal<Boolean> jtaAssociation = 
            new TransactionLocal<Boolean>() {
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };
    
    /**
     * Sets the transaction joined flag to {@code true} for the running 
     * transaction. This method has package private visibility.
     */
    static void setTransactionJoined() {
        jtaAssociation.set(true);
    }
    
    /**
     * Returns {@code true} if an persistence context is associated with the 
     * running transaction (if any), {@code false} otherwise.
     */
    public static boolean isTransactionJoined() {
        return jtaAssociation.get();
    }
    
    private static final ConcurrentMap<BeanletEntityManagerFactory, BeanletPersistenceContext> 
            transactionRegistry = new ConcurrentHashMap<BeanletEntityManagerFactory, BeanletPersistenceContext>();
    private static final ConcurrentMap<BeanletEntityManagerFactory, BeanletPersistenceContext> 
            extendedRegistry = new ConcurrentHashMap<BeanletEntityManagerFactory, BeanletPersistenceContext>();

    /**
     * Returns a persistence context for the specified arguments.
     */
    public static BeanletPersistenceContext getInstance(PersistenceContext pctx,
            ComponentUnit componentUnit) throws PersistenceException {
        final BeanletEntityManagerFactory emf = 
                BeanletEntityManagerFactoryRegistry.getInstance(
                pctx, componentUnit);
        if (emf.getPersistenceUnitInfo().getTransactionType() == 
                PersistenceUnitTransactionType.RESOURCE_LOCAL) {
            throw new PersistenceException("Container managed entity manager " +
                    "does not allow RESOURCE_LOCAL transaction type.");
        }
        if (pctx.type() == PersistenceContextType.TRANSACTION) {
            if (!transactionRegistry.containsKey(emf)) {
                if (transactionRegistry.putIfAbsent(emf, new BeanletPersistenceContext(emf, false)) == null) {
                    componentUnit.addDestroyHook(new Runnable() {
                        public void run() {
                            transactionRegistry.remove(emf);
                        }
                    });
                }
            }
            return transactionRegistry.get(emf);
        } else if (pctx.type() == PersistenceContextType.EXTENDED) {
            if (!extendedRegistry.containsKey(emf)) {
                if (extendedRegistry.putIfAbsent(emf, new BeanletPersistenceContext(emf, true)) == null) {
                    componentUnit.addDestroyHook(new Runnable() {
                        public void run() {
                            extendedRegistry.remove(emf);
                        }
                    });
                }
            }
            return extendedRegistry.get(emf);
        } else {
            throw new PersistenceException("PersistenceContextType not supported: " +
                    pctx.type() + ".");
        }
    }

    private final BeanletEntityManagerFactory emf;
    private final boolean extended;
    private final TransactionScopedEntityManager txem;
    
    private BeanletPersistenceContext(BeanletEntityManagerFactory emf,
            boolean extended) {
        this.emf = emf;
        this.extended = extended;
        if (!isExtended()) {
            this.txem = new TransactionScopedEntityManager(emf);
        } else {
            this.txem = null;
        }
    }

    /**
     * Returns {@code true} if this persistence context represents an extended
     * persistence context. Otherwise, {@code false} is returned.
     */
    public boolean isExtended() {
        return extended;
    }
    
    /**
     * Returns an {@code EntityManager} that can be passed to the client 
     * application. If {@code isExtended} returns {@code true}, this is an 
     * instance of {@code ExtendedEntityManager}, otherwise an instance of
     * {@code TransactionScopedEntityManager} is returned.
     */
    public EntityManager createEntityManager() {
        if (isExtended()) {
            return ExtendedEntityManager.getInstance(emf);
        } else {
            return txem;
        }
    }
    
    void preInvoke() {
        if (isExtended()) {
            ExtendedEntityManager.getInstance(emf).preInvoke();
        } else {
            txem.preInvoke();
        }
    }
    
    void postInvoke() {
        if (isExtended()) {
            ExtendedEntityManager.getInstance(emf).postInvoke();
        } else {
            txem.postInvoke();
        }
    }
}
