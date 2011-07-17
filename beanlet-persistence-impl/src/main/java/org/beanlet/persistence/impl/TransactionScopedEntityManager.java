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

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;
import java.util.Map;

/**
 * <ul>
 * <li>The application may obtain a container-managed entity manager with 
 * transaction-scoped persistence context bound to the JTA transaction by 
 * injection or direct lookup in the JNDI namespace. The persistence context 
 * type for the entity manager is defaulted or defined as PersistenceContext-
 * Type.TRANSACTION. (5.6.1 p121)
 * <li>A new persistence context begins when the container-managed entity 
 * manager is invoked in the scope of an active JTA transaction, and there is no 
 * current persistence context already associated with the JTA transaction. The 
 * persistence context is created and then associated with the JTA transaction. 
 * (5.6.1 p121)
 * <li>The persistence context ends when the associated JTA transaction commits 
 * or rolls back, and all entities that were managed by the EntityManager become 
 * detached. (5.6.1 p121)
 * <li>If the entity manager is invoked outside the scope of a transaction, 
 * any entities loaded from the database will immediately become detached at the 
 * end of the method call. (5.6.1 p121)
 * <li>The container must throw the {@code TransactionRequiredException} if
 * a transaction-scoped persistence context is used, and the 
 * {@code EntityManager} {@code persist}, {@code remove}, {@code merge}, or
 * {@code refresh} is invoked when no transaction is active. (5.9.1 p130)
 * </ul>
 *
 * @author Leon van Zantvoort
 */
public final class TransactionScopedEntityManager extends
        ContainerManagedEntityManager {

    private final InternalJTAEntityManagerFactory jta;
    private final InternalNonJTAEntityManagerFactory nonJTA;
    
    public TransactionScopedEntityManager(BeanletEntityManagerFactory emf) {
        this.nonJTA = new InternalNonJTAEntityManagerFactory(emf);
        this.jta = new InternalJTAEntityManagerFactory(emf);
    }
    
    public EntityManager getEntityManager() {
        final EntityManager em;
        if (jta.isTransactionActive()) {
            em = jta.createEntityManager();
        } else {
            em = nonJTA.createEntityManager();
        }
        return em;
    }    
    
    /**
     * This method has package private visibility.
     */
    @Override
    void preInvoke() {
        nonJTA.preInvoke();
        jta.preInvoke();
    }
    
    /**
     * This method has package private visibility.
     */
    @Override
    void postInvoke(boolean commit) {
        jta.postInvoke();
        nonJTA.postInvoke();
    }
    
    @Override
    public void persist(Object object) {
        verifyTransactionActive();
        super.persist(object);
    }
    
    @Override
    public void remove(Object object) {
        verifyTransactionActive();
        super.remove(object);
    }

    @Override
    public <T> T merge(T entity) {
        verifyTransactionActive();
        return super.merge(entity);
    }
    
    @Override
    public void refresh(Object object) {
        verifyTransactionActive();
        super.refresh(object);
    }
    
    @Override
    public boolean isOpen() {
        throw new IllegalStateException("Container managed transaction-scoped" +
                " entity manager does not allow this function.");
    }

    @Override
    public EntityTransaction getTransaction() {
        throw new IllegalStateException("Container managed transaction-scoped" +
                " entity manager does not allow this function.");
    }
    
    private void verifyTransactionActive() throws TransactionRequiredException {
        // PENDING: check if transaction is active?
        if (!jta.isTransactionActive()) {
            throw new TransactionRequiredException();
        }
    }

    public <T> T find(Class<T> cls, Object object) {
        verifyTransactionActive();
        return super.find(cls, object);
    }

    public <T> T getReference(Class<T> cls, Object object) {
        verifyTransactionActive();
        return super.getReference(cls, object);
    }

    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        verifyTransactionActive();
        return super.createNamedQuery(name, resultClass);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        verifyTransactionActive();
        return super.find(entityClass, primaryKey, properties);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        verifyTransactionActive();
        return super.find(entityClass, primaryKey, lockMode);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        verifyTransactionActive();
        return super.find(entityClass, primaryKey, lockMode, properties);
    }

    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        verifyTransactionActive();
        super.lock(entity, lockMode, properties);
    }

    public void refresh(Object entity, Map<String, Object> properties) {
        verifyTransactionActive();
        super.refresh(entity, properties);
    }

    public void refresh(Object entity, LockModeType lockMode) {
        verifyTransactionActive();
        super.refresh(entity, lockMode);
    }

    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        verifyTransactionActive();
        super.refresh(entity, lockMode, properties);
    }

    public void detach(Object entity) {
        verifyTransactionActive();
        super.detach(entity);
    }

    public LockModeType getLockMode(Object entity) {
        verifyTransactionActive();
        return super.getLockMode(entity);
    }

    public void setProperty(String propertyName, Object value) {
        verifyTransactionActive();
        super.setProperty(propertyName, value);
    }

    public Map<String, Object> getProperties() {
        verifyTransactionActive();
        return super.getProperties();
    }

    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        verifyTransactionActive();
        return super.createQuery(criteriaQuery);
    }

    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        verifyTransactionActive();
        return super.createQuery(qlString, resultClass);
    }

    public <T> T unwrap(Class<T> cls) {
        verifyTransactionActive();
        return super.unwrap(cls);
    }

    public EntityManagerFactory getEntityManagerFactory() {
        verifyTransactionActive();
        return super.getEntityManagerFactory();
    }

    public CriteriaBuilder getCriteriaBuilder() {
        verifyTransactionActive();
        return super.getCriteriaBuilder();
    }

    public Metamodel getMetamodel() {
        verifyTransactionActive();
        return super.getMetamodel();
    }
}
