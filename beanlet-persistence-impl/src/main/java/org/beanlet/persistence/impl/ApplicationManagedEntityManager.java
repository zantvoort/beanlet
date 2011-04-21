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
 * <li>When application-managed entity managers are used, the application must 
 * use the entity manager factory to manage the entity manager and persistence 
 * context lifecycle. (5.2 p114)
 * <li>An application-managed entity manager is obtained by the application from 
 * an entity manager factory. The EntityManagerFactory API used to obtain an 
 * application-managed entity manager is the same independent of whether this 
 * API is used in Java EE or Java SE environments. (5.2.2 p115)
 * <li>The EntityManagerFactory interface is used by the application to obtain 
 * an application-managed entity manager. When the application has finished 
 * using the entity manager factory, and/or at application shutdown, the 
 * application should close the entity manager factory. Once an 
 * EntityManagerFactory has been closed, all its entity managers are considered 
 * to be in the closed state. (5.4 p116)
 * <li>An application-managed entity manager may be either a JTA entity manager 
 * or a resource-local entity manager. (5.5 p118)
 * <li>All such application-managed persistence contexts are extended in scope, 
 * and may span multiple transactions. (5.7 p124)
 * <li>The extended persistence context exists from the point at which the 
 * entity manager has been created using 
 * EntityManagerFactory.createEntityManager until the entity manager is closed 
 * by means of EntityManager.close. The extended persistence context obtained 
 * from the application-managed entity manager is a stand-alone persistence 
 * context it is not propagated with the transaction. (5.7 p125)
 * <li>When a JTA application-managed entity manager is used, if the entity 
 * manager is created outside the scope of the current JTA transaction, it is 
 * the responsibility of the application to associate the entity manager with 
 * the transaction (if desired) by calling EntityManager.joinTransaction. 
 * (5.7 p125)
 * <li>When application-managed persistence contexts are used, the container 
 * must instantiate the entity manager factory and expose it to the application 
 * via JNDI. The container might use internal APIs to create the entity manager 
 * factory, or it might use the 
 * PersistenceProvider.createContainerEntityManagerFactory method. However, the 
 * container is required to support third-party persistence providers, and in 
 * this case the container must use the 
 * PersistenceProvider.createContainerEntityManagerFactory method to create the
 * entity manager factory and the EntityManagerFactory.close method to destroy 
 * the entity manager factory prior to shutdown (if it has not been previously 
 * closed by the application). (5.8.1 p129)
 * <li>If a persistence context is already associated with a JTA transaction, 
 * the container uses that persistence context for subsequent invocations within 
 * the scope of that transaction, according to the semantics for persistence 
 * context propagation defined in section 5.6.3. (5.8.2 p129)
 * </ul>
 * @author Leon van Zantvoort
 */
public abstract class ApplicationManagedEntityManager implements EntityManager {
    
    public abstract EntityManager getEntityManager();
    
    public void joinTransaction() {
        getEntityManager().joinTransaction();
    }

    public void clear() {
        getEntityManager().clear();
    }

    public void close() {
        getEntityManager().close();
    }

    public Query createNativeQuery(String string, String string0) {
        return getEntityManager().createNativeQuery(string, string0);
    }

    public void flush() {
        getEntityManager().flush();
    }

    public Object getDelegate() {
        return getEntityManager().getDelegate();
    }

    public FlushModeType getFlushMode() {
        return getEntityManager().getFlushMode();
    }

    public EntityTransaction getTransaction() {
        return getEntityManager().getTransaction();
    }

    public boolean isOpen() {
        return getEntityManager().isOpen();
    }

    public void remove(Object object) {
        getEntityManager().remove(object);
    }

    public void refresh(Object object) {
        getEntityManager().refresh(object);
    }

    public void persist(Object object) {
        getEntityManager().persist(object);
    }

    public boolean contains(Object object) {
        return getEntityManager().contains(object);
    }

    public Query createNamedQuery(String string) {
        return getEntityManager().createNamedQuery(string);
    }

    public Query createNativeQuery(String string) {
        return getEntityManager().createNativeQuery(string);
    }

    public Query createQuery(String string) {
        return getEntityManager().createQuery(string);
    }

    public void lock(Object object, LockModeType lockModeType) {
        getEntityManager().lock(object, lockModeType);
    }

    public Query createNativeQuery(String string, Class aClass) {
        return getEntityManager().createNativeQuery(string, aClass);
    }

    public void setFlushMode(FlushModeType flushModeType) {
        getEntityManager().setFlushMode(flushModeType);
    }

    public <T> T merge(T object) {
        return getEntityManager().merge(object);
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
}
