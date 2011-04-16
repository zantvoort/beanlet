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

/**
 * <p>Provides beanlet instances access to their runtime context.</p>
 * 
 * <p>The {@code BeanletContext} exposes information on the beanlet's definition
 * through the {@code BeanletMetaData} interface. Additionally, beanlets can 
 * obtain the {@code BeanletReference}, which is associated with beanlet method
 * calls.</p>
 *
 * <p>An instance of the {@code BeanletContext} can only be obtained through 
 * dependency injection.</p>
 * 
 * @param <T> the beanlet type.
 * @see Inject
 * @author Leon van Zantvoort
 */
public interface BeanletContext<T> {
    
    /**
     * Returns beanlet definition information for the underlying beanlet.
     *
     * @return meta data for the given underlying beanlet.
     */
    BeanletMetaData<T> getBeanletMetaData();
    
    /**
     * Returns the beanlet reference that is associated with the current call.
     *
     * @throws BeanletStateException thrown if no reference is associated with
     * current call. Note that references are only associated with calls on
     * proxy methods, or event executions for non static beanlets.
     */
    BeanletReference<T> reference() throws BeanletStateException;
}
