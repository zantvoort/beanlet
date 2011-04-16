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
package org.beanlet;

import java.util.Map;

/**
 * Factory for creating beanlet references.
 *
 * @author Leon van Zantvoort
 */
public interface BeanletFactory<T> {

    /**
     * Returns meta data for the underlying beanlet.
     */
    BeanletMetaData<T> getBeanletMetaData();

    /**
     * Creates a new beanlet reference for the underlying beanlet.
     *
     * @return a new beanlet reference for the underlying beanlet.
     * @throws BeanletCreationException if beanlet could not be created for
     * any reason.
     */
    BeanletReference<T> create() throws BeanletCreationException;    

    /**
     * Creates a new beanlet reference for the underlying beanlet.
     *
     * @param info map that contains parameters that can be injected into
     * the beanlet instance.
     * @return a new beanlet reference.
     * @throws BeanletCreationException if beanlet could not be created for
     * any reason.
     */
    BeanletReference<T> create(Map<String, ?> info) throws 
            BeanletCreationException;
}
