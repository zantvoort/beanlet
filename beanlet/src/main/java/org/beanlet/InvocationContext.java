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

import java.lang.reflect.Method;
import java.util.Map;

/**
 * <p>Allow interceptor and lifecycle methods to control the behavior of the 
 * invocation chain.</p>
 *
 * <p>The same {@code InvocationContext} instance is passed to each interceptor
 * method for a given business method or lifecycle event interception. This 
 * allows an interceptor to save information in the context data property of the
 * {@code InvocationContext} that can be subsequently retrieved in other 
 * interceptors as means to pass contextual data between interceptors. The
 * contextual data is not shareable across separate business method invocations
 * or lifecycle callback events. The lifecycle of the {@code InvocationContext}
 * is instance otherwise unspecified.</p>
 *
 * @author Leon van Zantvoort
 */
public interface InvocationContext {
    
    /**
     * Returns a map that can be used to pass contextual data between 
     * interceptors of the interceptor chain.
     */
    Map<String, Object> getContextData();
    
    /**
     * The method that is intercepted. This method does not return {@code null},
     * since only methods can be intercepted.
     * @throws BeanletStateException if this method is called from an lifecycle 
     * interceptor.
     */
    Method getMethod() throws BeanletStateException;
    
    /**
     * Returns the parameter of the current invocation.
     * @throws BeanletStateException if this method is called from an lifecycle 
     * interceptor.
     */
    Object[] getParameters() throws BeanletStateException;
    
    /**
     * Allows the caller to modify the parameters of the current invocation.
     * @throws BeanletStateException if this method is called from an lifecycle 
     * interceptor.
     */
    void setParameters(Object[] parameters) throws BeanletStateException;

    /**
     * Invokes the next interceptor in the chain, or, when called from the last
     * interceptor, the business method. Interceptor methods must always call
     * {@code InvocationContext.proceed()} or nu subsequent interceptor methods
     * or lifecycle callback methods or business method will be invoked. The
     * {@code proceed} method returns the result of the next method invoked. If
     * a method returns {@code void}, {@code proceed} returns {@code null}. For
     * lifecycle callback methods, if there is no callback method defined on the
     * beanlet class, the invocation of {@code proceed} in the last method in 
     * the chain is a on-op, and {@code null} is returned. If there is more than
     * one such method, the invocation of {@code proceed} causes the application
     * container to execute those methods in order.
     *
     * @return result of next method.
     */
    Object proceed() throws Exception;

    /**
     * Returns the underlying object.
     * 
     * @return the underlying object.
     */
    Object getTarget();
}
