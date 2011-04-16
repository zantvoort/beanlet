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

import javax.transaction.Status;

/**
 * @author Leon van Zantvoort
 */
public enum TransactionStatus {

    ACTIVE(Status.STATUS_ACTIVE),
    MARKED_ROLLBACK(Status.STATUS_MARKED_ROLLBACK),
    PREPARED(Status.STATUS_PREPARED),
    COMMITTED(Status.STATUS_COMMITTED),
    ROLLEDBACK(Status.STATUS_ROLLEDBACK),
    UNKNOWN(Status.STATUS_UNKNOWN),
    NO_TRANSACTION(Status.STATUS_NO_TRANSACTION),
    PREPARING(Status.STATUS_PREPARING),
    COMMITTING(Status.STATUS_COMMITTING),
    ROLLING_BACK(Status.STATUS_ROLLING_BACK);
    
    public static TransactionStatus toEnum(int status) {
        final TransactionStatus s;
        switch (status) {
            case Status.STATUS_ACTIVE:
                s = ACTIVE;
                break;
            case Status.STATUS_MARKED_ROLLBACK:
                s = MARKED_ROLLBACK;
                break;
            case Status.STATUS_PREPARED:
                s = PREPARED;
                break;
            case Status.STATUS_COMMITTED:
                s = COMMITTED;
                break;
            case Status.STATUS_ROLLEDBACK:
                s = ROLLEDBACK;
                break;
            case Status.STATUS_UNKNOWN:
                s = UNKNOWN;
                break;
            case Status.STATUS_NO_TRANSACTION:
                s = NO_TRANSACTION;
                break;
            case Status.STATUS_PREPARING:
                s = PREPARING;
                break;
            case Status.STATUS_COMMITTING:
                s = COMMITTING;
                break;
            case Status.STATUS_ROLLING_BACK:
                s = ROLLING_BACK;
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(status));
        }
        return s;
    }
    
    private TransactionStatus(int status) {
    }
}
