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

import static org.beanlet.transaction.impl.TransactionHelper.*;
import static org.beanlet.transaction.impl.TransactionStatus.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.beanlet.BeanletStateException;
import org.beanlet.common.InjectantImpl;
import org.jargo.ComponentContext;
import org.beanlet.Inject;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.common.AbstractDependencyInjectionFactory;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.Injectant;
import org.beanlet.transaction.TransactionAttribute;
import org.beanlet.transaction.TransactionContext;

/**
 *
 * @author Leon van Zantvoort
 */
public final class TransactionContextDependencyInjectionFactoryImpl extends
        AbstractDependencyInjectionFactory<Inject> {

    private final BeanletConfiguration<?> configuration;
    private final boolean containerManaged;
    
    public TransactionContextDependencyInjectionFactoryImpl(
            BeanletConfiguration<?> configuration) {
        super(configuration);
        this.configuration = configuration;
        this.containerManaged = !configuration.getAnnotationDomain().
                getDeclaration(TransactionAttribute.class).
                getElements().isEmpty();
    }
    
    public Class<Inject> annotationType() {
        return Inject.class;
    }

    public boolean isSupported(ElementAnnotation<? extends Element, Inject> ea) {
        return TransactionContext.class.isAssignableFrom(getType(ea));
    }
    
    public boolean isOptional(ElementAnnotation<? extends Element, Inject> ea) {
        return ea.getAnnotation().optional();
    }
    
    public Set<String> getDependencies(
            ElementAnnotation<? extends Element, Inject> ea) {
        Set<String> dependencies = new HashSet<String>();
        dependencies.addAll(getTransactionManagerDependencies());
        dependencies.addAll(getUserTransactionDependencies());
        return Collections.unmodifiableSet(dependencies);
    }

    public Injectant<?> getInjectant(
            final ElementAnnotation<? extends Element, Inject> ea, 
            final ComponentContext<?> ctx) {
        final TransactionManager transactionManager = getTransactionManager(
                configuration.getComponentName(),
                ea.getElement().getMember());
        final UserTransaction userTransaction = getUserTransaction(ctx,
                ea.getElement().getMember(), transactionManager);
        TransactionContext tx = new TransactionContext() {
            public boolean getRollbackOnly() throws BeanletStateException {
                try {
                    return transactionManager.getStatus() == 
                            Status.STATUS_MARKED_ROLLBACK;
                } catch (SystemException e) {
                    throw new BeanletStateException(
                            ctx.getComponentMetaData().getComponentName(), e);
                }
            }
            public void setRollbackOnly() throws BeanletStateException {
                try {
                    TransactionStatus status = TransactionStatus.toEnum(
                            transactionManager.getStatus());
                    if (status == ACTIVE || status == UNKNOWN) {
                        transactionManager.setRollbackOnly();
                    }
                } catch (IllegalStateException e) {
                    throw new BeanletStateException(
                            ctx.getComponentMetaData().getComponentName(), e);
                } catch (SystemException e) {
                    throw new BeanletStateException(
                            ctx.getComponentMetaData().getComponentName(), e);
                }
            }
            public UserTransaction getUserTransaction() throws 
                    BeanletStateException {
                if (containerManaged) {
                    throw new BeanletStateException(ctx.getComponentMetaData().getComponentName(),
                            "Method prohibited for container managed transactions.");
                }
                if (userTransaction == null) {
                    throw new BeanletStateException(ctx.getComponentMetaData().getComponentName(),
                            "UserTransaction not configured.");
                }
                return userTransaction;
            }
        };
        return new InjectantImpl<Object>(tx, true);
    }
    
    @Override
    public Class<?> getType(Inject inject) {
        return inject.type();
    }
}
