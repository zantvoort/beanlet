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
 * Classes implementing this interface can configured to intercept invocations. 
 * These classes are constructed by their (typically implicit) {@code public} 
 * accessible constructor. Example (A) shows how to implement an interceptor
 * using this interface. A more sophisticated interceptor is listed at example 
 * (B).
 * 
 * <p><h3>Examples</h3>
 * <b>(A)</b> Example of an interceptor implementing the {@code Interceptor} 
 * interface:
 * <pre>
 * public class ExampleInterceptor implements Interceptor {
 *
 *     public Object intercept(InvocationContext ctx) throws Exception {
 *         try {
 *             // Run code before the invocation...
 *             return ctx.proceed();
 *         } finally {
 *             // Run code after the invocation...
 *         }
 *     }
 * }
 * </pre>
 *
 * <b>(B)</b> Example of an interceptor that provides some very basic fail-over
 * functionality:
 * <pre>
 * public class ExampleInterceptor implements Interceptor {
 *     
 *     public Object intercept(InvocationContext ctx) throws Exception {
 *         try {
 *             return ctx.proceed();
 *         } catch (IOException e) {
 *             // Re-establish the connection.
 *             ((Foo) ctx.getTarget()).connect();
 *
 *             // Retry invocation.
 *             return ctx.proceed();
 *         }
 *     }
 * }
 * </pre>
 * </p>
 *
 * <p>A more powerful alternative to this interface is the {@link AroundInvoke} 
 * annotation.</p>
 *
 * @see AroundInvoke
 * @see Interceptors
 * @author Leon van Zantvoort
 */
public interface Interceptor {
    
    /**
     * <p>Intercepts an invocation.</p>
     */
    Object intercept(InvocationContext ctx) throws Exception;
}