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

import org.beanlet.plugin.TransactionLocal;

import java.util.Map;

/**
 * This class is used internally and should never be passed to the client 
 * application. This class has package private visibility.
 */
final class InternalEntityManager implements BeanletEntityManager {

    private final EntityManager em;
    private final TransactionLocal<Boolean> jtaAssociation = 
            new TransactionLocal<Boolean>() {
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    public InternalEntityManager(EntityManager em) {
        this.em = em;
    }

    public void persist(Object entity) {
        em.persist(entity);
    }

    public <T> T merge(T entity) {
        return em.merge(entity);
    }

    public void remove(Object entity) {
        em.remove(entity);
    }

    public <T> T find(Class<T> type, Object entity) {
        return em.find(type, entity);
    }

    public <T> T getReference(Class<T> type, Object entity) {
        return em.getReference(type, entity);
    }

    public void flush() {
        em.flush();
    }

    public void setFlushMode(FlushModeType flushModeType) {
        em.setFlushMode(flushModeType);
    }

    public FlushModeType getFlushMode() {
        return em.getFlushMode();
    }

    public void lock(Object object, LockModeType lockModeType) {
        em.lock(object, lockModeType);
    }

    public void refresh(Object entity) {
        em.refresh(entity);
    }

    public void clear() {
        em.clear();
    }

    public boolean contains(Object entity) {
        return em.contains(entity);
    }

    public Query createQuery(String query) {
        return em.createQuery(query);
    }

    public Query createNamedQuery(String query) {
        return em.createNamedQuery(query);
    }

    public Query createNativeQuery(String query) {
        return em.createNativeQuery(query);
    }

    public Query createNativeQuery(String query, Class type) {
        return em.createNativeQuery(query, type);
    }

    public Query createNativeQuery(String string, String string0) {
        return em.createNativeQuery(string, string0);
    }

    public void joinTransaction() {
        em.joinTransaction();
        jtaAssociation.set(true);
        BeanletPersistenceContext.setTransactionJoined();
    }

    public Object getDelegate() {
        return em.getDelegate();
    }

    public void close() {
        em.close();
    }

    public boolean isOpen() {
        return em.isOpen();
    }

    public EntityTransaction getTransaction() {
        return em.getTransaction();
    }

    public boolean isTransactionJoined() {
        return jtaAssociation.get();
    }

    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return em.createNamedQuery(name, resultClass);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return em.find(entityClass, primaryKey, properties);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return em.find(entityClass, primaryKey, lockMode);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return em.find(entityClass, primaryKey, lockMode, properties);
    }

    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        em.lock(entity, lockMode, properties);
    }

    public void refresh(Object entity, Map<String, Object> properties) {
        em.refresh(entity, properties);
    }

    public void refresh(Object entity, LockModeType lockMode) {
        em.refresh(entity, lockMode);
    }

    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        em.refresh(entity, lockMode, properties);
    }

    public void detach(Object entity) {
        em.detach(entity);
    }

    public LockModeType getLockMode(Object entity) {
        return em.getLockMode(entity);
    }

    public void setProperty(String propertyName, Object value) {
        em.setProperty(propertyName, value);
    }

    public Map<String, Object> getProperties() {
        return em.getProperties();
    }

    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return em.createQuery(criteriaQuery);
    }

    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return em.createQuery(qlString, resultClass);
    }

    public <T> T unwrap(Class<T> cls) {
        return em.unwrap(cls);
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return em.getEntityManagerFactory();
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return em.getCriteriaBuilder();
    }

    public Metamodel getMetamodel() {
        return em.getMetamodel();
    }
}