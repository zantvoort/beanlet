/*
 * GNU Lesser General Public License
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

import java.util.concurrent.atomic.AtomicReference;
import org.beanlet.BeanletStateException;
import static org.beanlet.WiringMode.*;
import static org.beanlet.transaction.impl.TransactionStatus.*;
import static java.util.logging.Level.*;
import java.util.IdentityHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.jargo.InvocationContext;
import org.jargo.InvocationInterceptor;
import org.beanlet.transaction.TransactionAttributeType;
import org.beanlet.transaction.TransactionSynchronization;
import org.jargo.ComponentConfiguration;

/**
 * <p>Intercepts business methods that are marked with the
 * {@code TransactionAttribute} annotation. This interceptor interacts with the
 * transaction manager according to the transaction attribute type specified
 * by this annotation.</p>
 *
 * @author Leon van Zantvoort
 */
public final class TransactionInvocationInterceptor implements
        InvocationInterceptor {
    
    private static final Logger logger = Logger.getLogger(
            TransactionInvocationInterceptor.class.getName());
    
    private final IdentityHashMap<Transaction, Object> registrations =
            new IdentityHashMap<Transaction, Object>();
    private static final Lock lock = new ReentrantLock(true);
    private static final Object DUMMY = new Object();
    
    private final ComponentConfiguration configuration;
    private final TransactionManager transactionManager;
    private final TransactionAttributeType type;
    private final int timeout;
    private final TransactionSynchronization synchronization;
    
    public TransactionInvocationInterceptor(
            ComponentConfiguration configuration,
            TransactionManager transactionManager,
            TransactionAttributeType type,
            int timeout,
            TransactionSynchronization synchronization) {
        this.configuration = configuration;
        this.transactionManager = transactionManager;
        this.type = type;
        this.timeout = timeout;
        this.synchronization = synchronization;
    }
    
    private void cleanRegistration(Transaction transaction) {
        lock.lock();
        try {
            registrations.remove(transaction);
        } finally {
            lock.unlock();
        }
    }
    
    private Transaction preTransaction(InvocationContext ctx) throws
            BeanletStateException {
        try {
            final Transaction transaction;
            transactionManager.setTransactionTimeout(timeout);
            TransactionStatus status = TransactionStatus.toEnum(
                    transactionManager.getStatus());
            switch (type) {
                case NEVER:
                    if (transactionManager.getTransaction() != null ||
                            status != NO_TRANSACTION) {
                        throw new BeanletStateException("Active transaction not " +
                                "allowed for attribute NEVER.");
                    }
                    if (synchronization != null) {
                        throw new BeanletStateException("TransactionSynchronization " +
                                "callback prohibited for transaction attribute NEVER.");
                    }
                    transaction = null;
                    break;
                case NOT_SUPPORTED:
                    if (transactionManager.getTransaction() != null ||
                            status != NO_TRANSACTION) {
                        transaction = transactionManager.suspend();
                        log("Suspended transaction " + transaction + ".", FINEST);
                    } else {
                        transaction = null;
                    }
                    if (synchronization != null) {
                        throw new BeanletStateException("TransactionSynchronization " +
                                "callback prohibited for transaction attribute NOT_SUPPORTED.");
                    }
                    break;
                case MANDATORY:
                    if (transactionManager.getTransaction() == null ||
                            status == NO_TRANSACTION) {
                        throw new BeanletStateException("Active transaction " +
                                "required for attribute MANDATORY.");
                    } else {
                        if (status == MARKED_ROLLBACK) {
                            log("Transaction is marked for rollback only.", INFO);
                        } else {
                            // Don't register if registration is fruitless b/c of status.
                            registerSynchronization(ctx);
                        }
                        transaction = null;
                    }
                    break;
                case SUPPORTS:
                    if (status == MARKED_ROLLBACK) {
                        log("Transaction is marked for rollback only.", INFO);
                    }
                    // Note that EJB does not allow synchronization for attribute SUPPORTS.
                    if (status == ACTIVE) {
                        // Don't register if registration is fruitless b/c of status.
                        registerSynchronization(ctx);
                    } else {
                        log("Skipping synchronization registration. " +
                                "Transaction is not active: " + status + ".", FINEST);
                    }
                    transaction = null;
                    break;
                case REQUIRED:
                    if (transactionManager.getTransaction() == null ||
                            status == NO_TRANSACTION) {
                        transactionManager.begin();
                        transaction = transactionManager.getTransaction();
                        log("Started transaction " + transaction + ".", FINEST);
                        registerSynchronization(ctx);
                    } else {
                        if (status == MARKED_ROLLBACK) {
                            log("Transaction is marked for rollback only.", INFO);
                        } else {
                            // Don't register if registration is fruitless b/c of status.
                            registerSynchronization(ctx);
                        }
                        transaction = null;
                    }
                    break;
                case REQUIRES_NEW:
                    if (transactionManager.getTransaction() != null ||
                            status != NO_TRANSACTION) {
                        transaction = transactionManager.suspend();
                        log("Suspended transaction " + transaction + ".", FINEST);
                    } else {
                        transaction = null;
                    }
                    transactionManager.begin();
                    log("Started transaction " + transactionManager.getTransaction() + ".", FINEST);
                    registerSynchronization(ctx);
                    break;
                default:
                    transaction = null;
                    assert false;
            }
            return transaction;
        } catch (BeanletStateException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanletStateException(configuration.
                    getComponentName(), e);
        }
    }
    
    private void postTransaction(Transaction transaction, boolean commit) throws
            BeanletStateException{
        if (transactionManager != null) {
            try {
                TransactionStatus status = TransactionStatus.toEnum(
                        transactionManager.getStatus());
                log("Transaction status: " + status + ".", FINEST);
                if (status == MARKED_ROLLBACK || status == ROLLEDBACK) {
                    commit = false;
                }
                switch (type) {
                    case NEVER:
                        break;
                    case NOT_SUPPORTED:
                        if (transaction != null) {
                            log("Resuming transaction " + transaction + ".", FINEST);
                            transactionManager.resume(transaction);
                        }
                        break;
                    case MANDATORY:
                        break;
                    case SUPPORTS:
                        break;
                    case REQUIRED:
                        if (transaction != null) {
                            try {
                                if (commit) {
                                    log("Committing transaction " + transaction + ".", FINEST);
                                    transactionManager.commit();
                                } else {
                                    log("Rolling back transaction " + transaction + ".", FINEST);
                                    transactionManager.rollback();
                                }
                            } catch (IllegalStateException e) {
                                // Ignore.
                            } finally {
                                // Disabled as this assertion might fail on application shutdown.
//                                assert transactionManager.getTransaction() == null ||
//                                        TransactionStatus.toEnum(transactionManager.getStatus()) == NO_TRANSACTION;
                            }
                        }
                        break;
                    case REQUIRES_NEW:
                        try {
                            if (commit) {
                                log("Committing transaction " + transactionManager.getTransaction() + ".", FINEST);
                                transactionManager.commit();
                            } else {
                                log("Rolling back transaction " + transactionManager.getTransaction() + ".", FINEST);
                                transactionManager.rollback();
                            }
                        } catch (IllegalStateException e) {
                            // Ignore.
                        } finally {
                            // Disabled as this assertion might fail on application shutdown.
//                            assert transactionManager.getTransaction() == null ||
//                                    TransactionStatus.toEnum(transactionManager.getStatus()) == NO_TRANSACTION;
                        }
                        if (transaction != null) {
                            log("Resuming transaction " + transaction + ".", FINEST);
                            transactionManager.resume(transaction);
                        }
                        break;
                    default:
                        assert false;
                }
            } catch (BeanletStateException e) {
                throw e;
            } catch (Exception e) {
                throw new BeanletStateException(configuration.
                        getComponentName(), e);
            }
        }
    }
    
    public boolean isLifecycleInterceptor() {
        return false;
    }
    
    public Object intercept(InvocationContext ctx) throws Exception {
        Transaction transaction = null;
        boolean commit = false;
        try {
            if (transactionManager != null) {
                log("Intercepting " + ctx.getInvocation().getMethod().getName() +
                        " with attribute " + type + ".", FINEST);
                transaction = preTransaction(ctx);
            } else {
                transaction = null;
            }
            Object result = ctx.proceed();
            commit = true;
            return result;
        } finally {
            if (transactionManager != null) {
                postTransaction(transaction, commit);
            }
        }
    }
    
    private void registerSynchronization(final InvocationContext ctx) throws
            IllegalStateException, RollbackException, SystemException {
        assert transactionManager != null;
        final Transaction transaction = transactionManager.getTransaction();
        assert transaction != null;
        lock.lock();
        try {
            if (registrations.put(transaction, DUMMY) == null) {
                log("Registering transaction: " + transaction + ".", FINEST);
                boolean registered = false;
                TransactionLocalDelegateImpl.begin(transaction);
                try {
                    try {
                        if (synchronization != null) {
                            synchronization.afterBegin();
                        }
                    } finally {
                        transaction.registerSynchronization(new Synchronization() {
                            // Hold a strong reference to the component to prevent it from being destroyed.
                            AtomicReference<?> reference = 
                                    new AtomicReference<Object>(
                                    ctx.getComponentContext().reference());
                            public void beforeCompletion() {
                                if (synchronization != null) {
                                    synchronization.beforeCompletion();
                                }
                            }
                            public void afterCompletion(int i) {
                                try {
                                    if (synchronization != null) {
                                        synchronization.afterCompletion(i == Status.STATUS_COMMITTED);
                                    }
                                } finally {
                                    reference.set(null);
                                    try {
                                        log("Deregistering transaction on" +
                                                " completion: " + transaction + ".", FINEST);
                                        cleanRegistration(transaction);
                                    } finally {
                                        TransactionLocalDelegateImpl.commit(transaction);
                                    }
                                }
                            }
                        });
                        registered = true;
                    }
                } catch (Throwable t) {
                    if (logger != null) {
                        logger.log(WARNING, "Failed to register " +
                                "synchronization for transaction: " + 
                                transaction + ".", t);
                    }
                    try {
                        throw t;
                    } catch (IllegalStateException e) {
                        throw e;
                    } catch (RollbackException e) {
                        // Note that in case of Jotm, the 
                        // registerSynchronization method does not fail 
                        // atomically. If this exception is thrown, 
                        // the synchronization methods stil seem to be executed.
                        throw e;
                    } catch (SystemException e) {
                        throw e;
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        assert false;
                        throw new RuntimeException(e);
                    } catch (Error e) {
                        throw e;
                    } catch (Throwable e) {
                        assert false : e;
                        throw new RuntimeException(e);
                    }
                } finally {
                    if (!registered) {
                        if (logger != null) {
                            logger.finest("Deregistering transaction because " +
                                    "synchronization registration failed: " + 
                                    transaction + ".");
                        }
                        cleanRegistration(transaction);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }
    
    private void log(String msg, Level level) {
        if (logger != null && logger.isLoggable(level)) {
            logger.log(level, msg);
        }
    }
}
