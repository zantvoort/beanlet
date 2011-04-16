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
package org.beanlet.transaction.impl;

import java.lang.reflect.Member;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.beanlet.BeanletApplicationContext;
import org.beanlet.BeanletTypeIsDuplicateException;
import org.beanlet.BeanletTypeNotFoundException;
import org.jargo.ComponentContext;
import org.jargo.ComponentReference;

/**
 *
 * @author Leon van Zantvoort
 */
final class TransactionHelper {

    private static final BeanletApplicationContext ctx = 
            BeanletApplicationContext.instance();
    
    private static final AtomicReference<Set<String>> txdeps = 
            new AtomicReference<Set<String>>();
    private static final AtomicReference<Set<String>> utdeps = 
            new AtomicReference<Set<String>>();
    private static final AtomicReference<TransactionManager> tx = 
            new AtomicReference<TransactionManager>();
    
    private TransactionHelper() {
    }

    public static Set<String> getTransactionManagerDependencies() {
        Set<String> deps = txdeps.get();
        if (deps == null) {
            deps = ctx.getBeanletNamesForType(TransactionManager.class, true, false);
            txdeps.set(deps);
        }
        return deps;
    }

    public static Set<String> getUserTransactionDependencies() {
        Set<String> deps = utdeps.get();
        if (deps == null) {
            deps = ctx.getBeanletNamesForType(UserTransaction.class, true, false);
            utdeps.set(deps);
        }
        return deps;
    }

    public static TransactionManager getTransactionManager(String beanletName,
            Member member) {
        TransactionManager t = tx.get();
        if (t == null) {
            Set<String> names = BeanletApplicationContext.instance().
                    getBeanletNamesForType(TransactionManager.class, true, true);
            if (names.isEmpty()) {
                throw new BeanletTypeNotFoundException(beanletName, member, 
                        TransactionManager.class);
            }
            if (names.size() > 1) {
                throw new BeanletTypeIsDuplicateException(beanletName, member, 
                        TransactionManager.class, names);
            }
            t = BeanletApplicationContext.instance().getBeanlet(
                names.iterator().next(), TransactionManager.class);
            tx.set(t);
        }
        return t;
    }
    
    public static UserTransaction getUserTransaction(ComponentContext<?> ctx,
            Member member, final TransactionManager tm) {
        Set<String> names = BeanletApplicationContext.instance().
                getBeanletNamesForType(UserTransaction.class, true, true);
        if (names.isEmpty()) {
            // This is allowed.
            return null;
            
            // Use the following line instead to enforce the availability of UserTransaction. 
//            throw new BeanletTypeNotFoundException(
//                    ctx.getComponentMetaData().getComponentName(),
//                    getMember(ea.getElement()), UserTransaction.class);
        }
        if (names.size() > 1) {
            throw new BeanletTypeIsDuplicateException(ctx.getComponentMetaData().
                    getComponentName(), member, UserTransaction.class, names);
        }
        final UserTransaction ut = BeanletApplicationContext.instance().
                getBeanlet(names.iterator().next(), UserTransaction.class);
        final ComponentReference<?> reference = ctx.reference().weakReference();
        return new UserTransaction() {
            public void begin() throws NotSupportedException,SystemException {
                ut.begin();
                Transaction tx = tm.getTransaction();
                assert tx != null;
                TransactionLocalDelegateImpl.begin(tx);
                TransactionLocalDelegateImpl.userTransaction(reference);
            }
            public void commit() throws RollbackException,HeuristicMixedException,HeuristicRollbackException,SecurityException,IllegalStateException,SystemException {
                Transaction tx = tm.getTransaction();
                assert tx != null;
                ut.commit();
                TransactionLocalDelegateImpl.commit(tx);
            }
            public int getStatus() throws SystemException {
                return ut.getStatus();
            }
            public void rollback() throws IllegalStateException,SecurityException,SystemException {
                Transaction tx = tm.getTransaction();
                assert tx != null;
                ut.rollback();
                TransactionLocalDelegateImpl.commit(tx);
            }
            public void setRollbackOnly() throws IllegalStateException,SystemException {
                ut.setRollbackOnly();
            }
            public void setTransactionTimeout(int i) throws SystemException {
                ut.setTransactionTimeout(i);
            }
        };
    }
}
