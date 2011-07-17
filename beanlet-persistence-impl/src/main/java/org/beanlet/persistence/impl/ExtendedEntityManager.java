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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;
import javax.transaction.Status;
import javax.transaction.Transaction;
import org.beanlet.plugin.TransactionLocal;
import org.jargo.ComponentApplicationContext;
import org.jargo.ComponentReference;
import org.jargo.MetaData;

/**
 * <ul>
 * <li>A container-managed extended persistence context can only be initiated 
 * within the scope of a stateful session bean. It exists from the point at 
 * which the stateful session bean that declares a dependency on an entity 
 * manager of type PersistenceContextType.EXTENDED is created, and is said to be 
 * bound to the stateful session bean. The dependency on the extended 
 * persistence context is declared by means of the PersistenceContext annotation 
 * or persistence-context-ref deployment descriptor element. (5.6.2 p121)
 * <li>The persistence context is closed by the container when the @Remove 
 * method of the stateful session bean completes (or the stateful session bean 
 * instance is otherwise destroyed). (5.6.2 p121)
 * <li>If a stateful session bean instantiates a stateful session bean which 
 * also has such an extended persistence context, the extended persistence 
 * context of the first stateful session bean is inherited by the second 
 * stateful session bean and bound to it, and this rule recursively 
 * applies independently of whether transactions are active or not at the point 
 * of the creation of the stateful session beans. (5.6.2.1 p121)
 * <li>If the persistence context has been inherited by any stateful session 
 * beans, the container does not close the persistence context until all such 
 * stateful session beans have been removed or otherwise destroyed. 
 * (5.6.2.1 p121)
 * </ul>
 *
 * @author Leon van Zantvoort
 */
public final class ExtendedEntityManager extends ContainerManagedEntityManager {

    private static final Map<ComponentReference, ExtendedEntityManager> registry =
            new HashMap<ComponentReference, ExtendedEntityManager>();
    private static final Map<ExtendedEntityManager, AtomicInteger> counter = 
            new HashMap<ExtendedEntityManager, AtomicInteger>();
    private static final TransactionLocal<?> txLocal = new TransactionLocal<Object>();
    
    private static final ComponentApplicationContext ctx = ComponentApplicationContext.instance();
    private static final Logger logger = Logger.getLogger(ExtendedEntityManager.class.getName());
    
    private static boolean hasExtendedPersistenceContext(ComponentReference<?> reference) {
        for (MetaData m : reference.getComponentMetaData().getMetaData()) {
            if (m instanceof PersistenceContextMetaData) {
                PersistenceContext ctx = ((PersistenceContextMetaData) m).
                        getPersistenceContext();
                if (ctx.type() == PersistenceContextType.EXTENDED) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static synchronized ExtendedEntityManager getInstance(BeanletEntityManagerFactory emf) {
        List<ComponentReference<?>> callStack = ctx.referenceStack();
        assert !callStack.isEmpty();
        Iterator<ComponentReference<?>> i = callStack.iterator();
        ComponentReference<?> current = i.next().weakReference();
        assert !current.getComponentMetaData().isStatic();
        
        final ExtendedEntityManager em;
        if (registry.containsKey(current)) {
            em = registry.get(current);
        } else {
            // No em registered for current reference; lookup root pctx reference.
            ComponentReference<?> root = current;
            while (i.hasNext()) {
                ComponentReference<?> prev = i.next().weakReference();
                if (!prev.getComponentMetaData().isStatic()) {
                    if (hasExtendedPersistenceContext(prev)) {
                        root = prev;
                    }
                } else {
                    // Stateful chain broken.
                    break;
                }
            }
            if (current == root) {
                logger.finest("Creating new extended persistence context for " + root + ".");
                em = new ExtendedEntityManager(emf.createEntityManager());
                registry.put(root, em);
                counter.put(em, new AtomicInteger(1));
                txLocal.onUserTransaction(root.weakReference(), new Runnable() {
                    public void run() {
                        em.join();
                    }
                });
                registerDestroyHook(root, em);
            } else {
                if (registry.containsKey(root)) {
                    em = registry.get(root);
                    logger.finest(current + " inherits extended persistence context from " + root + ".");
                    registry.put(current, em);
                    AtomicInteger ai = counter.get(current);
                    assert ai != null;
                    ai.incrementAndGet();
                    txLocal.onUserTransaction(current.weakReference(), new Runnable() {
                        public void run() {
                            em.join();
                        }
                    });
                    registerDestroyHook(current, em);
                } else {
                    logger.finest("Creating new extended persistence context for " + root + ".");
                    em = new ExtendedEntityManager(emf.createEntityManager());
                    registry.put(root, em);
                    counter.put(em, new AtomicInteger(2));
                    txLocal.onUserTransaction(root.weakReference(), new Runnable() {
                        public void run() {
                            em.join();
                        }
                    });
                    registerDestroyHook(root, em);
                    logger.finest(current + " inherits extended persistence context from " + root + ".");
                    registry.put(current, em);
                    txLocal.onUserTransaction(current.weakReference(), new Runnable() {
                        public void run() {
                            em.join();
                        }
                    });
                    registerDestroyHook(current, em);
                }
            }
        }
        assert em != null;
        return em;
    }
    
    private static void registerDestroyHook(final ComponentReference<?> reference, 
            final ExtendedEntityManager em) {
        reference.addDestroyHook(new Runnable() {
            public void run() {
                ExtendedEntityManager tmp = registry.remove(reference);
                assert tmp == em;
                AtomicInteger ai = counter.get(em);
                assert ai != null;
                if (ai.decrementAndGet() == 0) {
                    counter.remove(em);
                    em.getEntityManager().close();
                }
            }
        });
    }
    
    private final BeanletEntityManager em;
    
    public ExtendedEntityManager(BeanletEntityManager em) {
        this.em = em;
    }
    
    public EntityManager getEntityManager() {
        return em;
    }

    public <T> T find(Class<T> cls, Object object) {
        return getEntityManager().find(cls, object);
    }

    public <T> T getReference(Class<T> cls, Object object) {
        return getEntityManager().getReference(cls, object);
    }

    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return getEntityManager().createNamedQuery(name, resultClass);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return getEntityManager().find(entityClass, primaryKey, properties);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return getEntityManager().find(entityClass, primaryKey, lockMode);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return getEntityManager().find(entityClass, primaryKey, lockMode, properties);
    }

    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        getEntityManager().lock(entity, lockMode, properties);
    }

    public void refresh(Object entity, Map<String, Object> properties) {
        getEntityManager().refresh(entity, properties);
    }

    public void refresh(Object entity, LockModeType lockMode) {
        getEntityManager().refresh(entity, lockMode);
    }

    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        getEntityManager().refresh(entity, lockMode, properties);
    }

    public void detach(Object entity) {
        getEntityManager().detach(entity);
    }

    public LockModeType getLockMode(Object entity) {
        return getEntityManager().getLockMode(entity);
    }

    public void setProperty(String propertyName, Object value) {
        getEntityManager().setProperty(propertyName, value);
    }

    public Map<String, Object> getProperties() {
        return getEntityManager().getProperties();
    }

    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return getEntityManager().createQuery(criteriaQuery);
    }

    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return getEntityManager().createQuery(qlString, resultClass);
    }

    public <T> T unwrap(Class<T> cls) {
        return getEntityManager().unwrap(cls);
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return getEntityManager().getEntityManagerFactory();
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return getEntityManager().getCriteriaBuilder();
    }

    public Metamodel getMetamodel() {
        return getEntityManager().getMetamodel();
    }

    /**
     * Returns {@code true} if running thread is participating in an active
     * transaction.
     *
     * @throws PersistenceException if failed to obtain transaction status.
     */
    private boolean isTransactionActive() throws PersistenceException {
        try {
            Transaction tx = txLocal.getTransaction();
            return tx != null && tx.getStatus() == Status.STATUS_ACTIVE;
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }
    
    /**
     * This method has package private visibility.
     */
    @Override
    void preInvoke() {
        join();
    }
    
    /**
     * This method has package private visibility.
     */
    @Override
    void postInvoke(boolean commit) {
        // Do nothing.
    }
    
    private void join() {
        if (isTransactionActive()) {
            if (!em.isTransactionJoined()) {
                if (BeanletPersistenceContext.isTransactionJoined()) {
                    throw new IllegalStateException("Different persistence " +
                            "context already associated with the JTA transaction.");
                }
                em.joinTransaction();
                logger.finest("Extended entity manager joined transaction: " +
                        this + ".");
            } else {
                logger.finest("Extended entity manager already joined transaction: " +
                        this + ".");
            }
        } else {
            logger.finest("Entity manager did not join transaction. " +
                    "No transaction active: " + this + ".");
        }
    }
}
