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
 * <p>Represents a reference to a beanlet.</p>
 * 
 * <p>Beanlets references are used to uniquely identify the underlying beanlet
 * and to control the lifecycle of the beanlet.</p>
 * 
 * <p>Invocations made on a beanlet proxy that points to a stateful beanlet are
 * always performed on the same beanlet instance. In case of a stateless 
 * beanlet, it is not specified which beanlet instance is used to execute the 
 * invocation.</p>
 * 
 * <p>Beanlet references can be removed, either explicitly by calling 
 * {@link #invalidate} or alternatively, once it is no longer stronly reachable, 
 * by the garbage collector. Beanlet references that have been removed can no
 * longer be used to perform calls on the underlying beanlet. Such calls will
 * result in an unchecked exception thrown by the application container. The 
 * {@link #isRemoved} method can be used to check whether a reference has 
 * been removed.</p>
 * 
 * @author Leon van Zantvoort
 */
public interface BeanletReference<T> {

    /**
     * Returns meta data for the underlying beanlet.
     */
    BeanletMetaData<T> getBeanletMetaData();
    
    /**
     * Returns the beanlet.
     *
     * @return the beanlet.
     */
    Object getBeanlet();

    /**
     * Returns the beanlet if this object is an instance of the beanlet type
     * as specified by {@link BeanletMetaData#getType}. This might not always 
     * be the case, for example for proxy beanlets. In this case a 
     * {@code BeanletNotOfRequiredTypeException} exception is thrown.
     *
     * @return the beanlet.
     * @throws BeanletNotOfRequiredTypeException if beanlet is not an instance
     * of beanlet type.
     */
    T getTypedBeanlet() throws BeanletNotOfRequiredTypeException;
    
    /**
     * Returns {@code true} if specified {@code event} is supported by this
     * reference.
     */
    boolean isExecutable(Event event);
    
    /**
     * Executes the specified {@code event}.
     * 
     * @throws BeanletEventException result of event execution.
     */
    Object execute(Event event) throws BeanletEventException;
    
    /**
     * Causes this reference to be invalidated and removed. As a result,
     * {@code isValid} returns {@code false}. Events can still be executed
     * while the reference is being invalidated.
     * 
     * This method is idempotent.
     */
    void invalidate();
    
    /**
     * Returns {@code true} if this reference if valid. If {@code false} is 
     * returned, the reference is being destroyed and finally removed, resulting
     * in {@code isRemoved} to return {@code true}.
     *
     * @return {@code true} if this reference is valid, {@code false} otherwise.
     */
    boolean isValid();
    
    /**
     * Remove the reference immediately. The reference is not being invalidated.
     */
    void remove();
    
    /**
     * Returns {@code true} if this reference has been removed, it
     * can no longer execute events, {@code false} otherwise.
     *
     * @return {@code true} if this reference is removed, {@code false} otherwise.
     */
    boolean isRemoved();
}
