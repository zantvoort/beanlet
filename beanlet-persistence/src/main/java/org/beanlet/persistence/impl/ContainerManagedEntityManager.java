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
 * <li>When container-managed entity managers are used 
 * (in Java EE environments), the application does not interact with the 
 * entity manager factory. The entity managers are obtained directly through 
 * dependency injection or from JNDI, and the container manages interaction with 
 * the entity manager factory transparently to the application. (5.2 p114)
 * <li>The container manages the persistence context lifecycle and the creation 
 * and the closing of the entity manager instance transparently to the 
 * application. (5.2.1 p114)
 * <li>A container-managed entity manager must be a JTA entity manager. JTA 
 * entity managers are only specified for use in Java EE containers. (5.5 p118)
 * <li>When a container-managed entity manager is used, the lifecycle of the 
 * persistence context is always managed automatically, transparently to the 
 * application, and the persistence context is propagated with the JTA 
 * transaction. (5.6 p120)
 * <li>A container-managed persistence context may be defined to have either a 
 * lifetime that is scoped to a single transaction or an extended lifetime that 
 * spans multiple transactions, depending on the PersistenceContextType that is 
 * specified when its EntityManager is created. This specification refers to
 * such persistence contexts as transaction-scoped persistence contexts and 
 * extended persistence contexts respectively. (5.6 p120)
 * <li>Persistence contexts are always associated with an entity manager 
 * factory. (5.6 p121)
 * <li>As described in section 5.1, a single persistence context may correspond 
 * to one or more JTA entity manager instances (all associated with the same 
 * entity manager factory). The persistence context is propagated across the 
 * entity manager instances as the JTA transaction is propagated. (5.6.3 p121)
 * <li>Persistence contexts are propagated by the container across component 
 * invocations as follows. 
 * 
 * If a component is called and there is no JTA transaction or the JTA 
 * transaction is not propagated, the persistence context is not propagated.
 *   <ul>
 *   <li>If an entity manager is then invoked from within the component:
 *     <ul>
 *     <li>Invocation of an entity manager defined with PersistenceContext-
 *     Type.TRANSACTION will result in use of a new persistence context (as 
 *     described in section 5.6.1).
 *     <li>Invocation of an entity manager defined with PersistenceContext-
 *     Type.EXTENDED will result in the use of the existing extended persistence 
 *     context bound to that component.
 *     <li>If the entity manager is invoked within a JTA transaction, the 
 *     persistence context will be bound to the JTA transaction.
 *     </ul>
 *   <li>If a component is called and the JTA transaction is propagated into 
 *   that component:
 *     <ul>
 *     <li>If the component is a stateful session bean to which an extended 
 *     persistence context has been bound and there is a different persistence 
 *     context bound to the JTA transaction, an EJBException is thrown by the 
 *     container.
 *     <li>Otherwise, if there is a persistence context bound to the JTA 
 *     transaction, that persistence context is propagated and used.
 *     </ul>
 *   </ul>
 *   (5.6.3.1 p122)
 * <li>Entity manager instances obtained from different entity manager factories 
 * never share the same persistence context. (p122)
 * <li>For the management of a transaction-scoped persistence context, if there 
 * is no EntityManager already associated with the JTA transaction:
 *   <ul>
 *   <li>The container creates a new entity manager by calling 
 *   EntityManagerFactory.createEntityManager when the first invocation of an 
 *   entity manager with Persistence-ContextType.TRANSACTION occurs within the 
 *   scope of a business method executing in the JTA transaction.
 *   <li>After the JTA transaction has completed (either by transaction commit 
 *   or rollback), The container closes the entity manager by calling 
 *   EntityManager.close.
 *   </ul>
 *   The container must throw the TransactionRequiredException if a 
 *   transaction-scoped persistence context is used, and the EntityManager 
 *   persist, remove, merge, or refresh method is invoked when no transaction 
 *   is active.<br>
 *   <br>
 *   For stateful session beans with extended persistence contexts:
 *   <ul>
 *   <li>The container creates an entity manager by calling 
 *   EntityManagerFactory.createEntityManager when a stateful session bean is 
 *   created that declares a dependency on an entity manager with 
 *   PersistenceContextType.EXTENDED. (See section 5.6.2).
 *   <li>The container closes the entity manager by calling EntityManager.close 
 *   after the stateful session bean and all other stateful session beans that 
 *   have inherited the same persistence context as the EntityManager have been 
 *   removed.
 *   <li>When a business method of the stateful session bean is invoked, if the 
 *   stateful session bean uses container managed transaction demarcation, and 
 *   the entity manager is not already associated with the current JTA 
 *   transaction, the container associates the entity manager with the current
 *   JTA transaction and calls EntityManager.joinTransaction. If there is a
 *   different persistence context already associated with the JTA transaction, 
 *   the container throws the EJBException.
 *   <li>When a business method of the stateful session bean is invoked, if the 
 *   stateful session bean uses bean managed transaction demarcation and a 
 *   UserTransaction is begun within the method, the container associates the 
 *   persistence context with the JTA transaction and calls 
 *   EntityManager.joinTransaction.
 *   </ul>
 *   (5.9.1 p129)
 * <li>The container must throw the IllegalStateException if the application 
 * calls EntityManager. close on a container-managed entity manager. 
 * (5.9.1 p130)
 * <li>When the container creates an entity manager, it may pass a map of 
 * properties to the persistence provider by using the 
 * EntityManagerFactory.createEntityManager(Map map) method. If properties have 
 * been specified in the PersistenceContext annotation or the 
 * persistence-context-ref deployment descriptor element, this method must be 
 * used and the map must include the specified properties. (5.9.1 p130)
 * </ul>
 * @author Leon van Zantvoort
 */
abstract class ContainerManagedEntityManager implements EntityManager {
    
    abstract EntityManager getEntityManager();

    abstract void preInvoke();

    abstract void postInvoke();
    
    public void clear() {
        getEntityManager().clear();
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

    public void joinTransaction() {
        getEntityManager().joinTransaction();
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
    
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return getEntityManager().getReference(entityClass, primaryKey);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return getEntityManager().find(entityClass, primaryKey);
    }
    
    public <T> T merge(T entity) {
        return getEntityManager().merge(entity);
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

    public void close() {
        throw new IllegalStateException("Container managed entity manager " +
                "does not allow this function.");
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
