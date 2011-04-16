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
package org.beanlet.persistence.hibernate;

import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.beanlet.BeanletApplicationContext;
import org.beanlet.BeanletException;
import org.beanlet.BeanletNotActiveException;
import org.hibernate.HibernateException;
import org.hibernate.transaction.TransactionManagerLookup;

/**
 * @author Leon van Zantvoort
 */
public class HibernateTransactionManagerLookup implements TransactionManagerLookup {

    private static final Logger logger = Logger.getLogger(
            HibernateTransactionManagerLookup.class.getName());
    
    public TransactionManager getTransactionManager(Properties properties) throws HibernateException {
        final String name;
        final TransactionManager tx;
        try {
            BeanletApplicationContext ctx = BeanletApplicationContext.instance();
            Set<String> names = ctx.getBeanletNamesForType(
                    TransactionManager.class, true, true);
            if (names.isEmpty()) {
                logger.info("No beanlet TransactionManager configured. " +
                        "No TransactionManager is used for this Hibernate instance.");
                return null;
            } else if (names.size() > 1) {
                throw new HibernateException("Multiple beanlet TransactionManagers " +
                        "configured: " + names + ".");
            } else {
                try {
                    name = names.iterator().next();
                    tx = ctx.getBeanlet(name, TransactionManager.class);
                } catch (BeanletNotActiveException e) {
                    logger.info("Beanlet TransactionManager not active: '" + e.getBeanletName() + "'. " +
                            "No TransactionManager is used for this Hibernate instance.");
                    // We don't need to rethrow this exception.
                    return null;
                } catch (BeanletException e) {
                    throw new HibernateException("Cannot obtain TransactionManager.", e);
                }
            }
        } catch (HibernateException e) {
            logger.log(Level.WARNING, "THROW", e);
            throw e;
        }
        logger.finest("Successfully obtained TransactionManager: '" + name + "'.");
        return tx;
    }
    
    public String getUserTransactionName() {
        // PENDING: isn't this already covered by beanlet?
        return null;
    }

    public Object getTransactionIdentifier(Transaction transaction) {
        return transaction;
    }
}
