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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Designates a method to intercept invocations on a beanlet. A class that
 * declares a method marked with the {@code ArroundInvoke} annotation is said to
 * be an interceptor class. Interceptor classes are instantiated either by their
 * (typically implicit) sole constructor, or by contructor injection.
 * Example (A) shows how to implement an interceptor using this annotation.</p>
 * 
 * <p><h3>Inner Interceptors</h3>
 * The {@code ArroundInvoke} annotation can be applied on methods of any class,
 * including classes implementing a beanlet. Such interceptors are so-called
 * inner interceptors. An inner interceptor is <u>automatically</u>
 * installed for all business methods of a beanlet. It is placed at the tail of 
 * the interceptor chain. It cannot be excluded by the 
 * {@code ExcludeClassInterceptors} annotation. An example of an inner
 * interceptor can be found at example (B).</p>
 * 
 * <p><h3>Hierarchical Interceptors</h3>
 * Interceptor classes may declare only one interceptor method. However 
 * interceptor methods may be defined on any of its superclasses. The 
 * interceptor methods defined by these superclasses are invoked before the 
 * interceptor method defined by the interceptor class, most general superclass
 * first. This is demonstrated by example (C).<br>
 * If a interceptor method is overridden by another method (regardless whether 
 * that method is itself an interceptor method), it will not be invoked. An
 * example of such an interceptor is listed at (D).<br>
 * </p>
 *
 * <p><h3>Method Constraints</h3>
 * The method on which the {@code AroundInvoke} annotation is applied MUST 
 * fulfill all of the following criteria:
 * <ul>
 * <li>The method MUST have the {@code InterceptorContext} object as parameter.
 * <li>The return type of the method MUST NOT be {@code void}
 * <li>The method MAY throw a checked exception.
 * <li>The method on which {@code AroundInvoke} is applied MAY be 
 * {@code public}, {@code protected}, package private or {@code private}. 
 * <li>The method MUST NOT be {@code static}.
 * <li>The method MAY be {@code final}. 
 * </ul>
 * </p>
 *
 * <p><h3>Examples</h3>
 * <b>(A)</b> Example of an interceptor class using the {@code ArroundInvoke} 
 * annotation:
 * <pre>
 * public class BaseInterceptor {
 *
 *     &#064;AroundInvoke
 *     public Object wrap(InvocationContext ctx) throws Exception {
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
 * <b>(B)</b> Example of a beanlet with an inner interceptor:
 * <pre>
 * public class ExampleBeanlet {
 *     
 *     &#64;Factory
 *     public Object getInstance() {
 *         return new Object();
 *     }
 *
 *     &#64;ArroundInvoke 
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
 * <b>(C)</b> Example of an interceptor class that extends the interceptor class
 * listed at example (A). The {@code BaseInterceptor.wrap()} method preceeds
 * the {@code ExtendedInterceptor.wrapToo()} method in the interceptor chain:
 * <pre>
 * public class ExtendedInterceptor extends BaseInterceptor {
 *
 *     &#064;AroundInvoke 
 *     public Object wrapToo(InvocationContext ctx) throws Exception {
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
 * <b>(D)</b> Example of an extended interceptor class of which the interceptor
 * method overrides the interceptor method of its superclass (listed at (A)). 
 * The {@code BaseInterceptor.wrap()} method is excluded from the interceptor
 * chain, it is not invoked. The {@code ExtendedInterceptor.wrap()} method 
 * however, is included in the interceptor chain.
 * <pre>
 * public class ExtendedInterceptor extends BaseInterceptor {
 *
 *     &#064;AroundInvoke 
 *     public Object wrap(InvocationContext ctx) throws Exception {
 *         try {
 *             // Run code before the invocation...
 *             return ctx.proceed();
 *         } finally {
 *             // Run code after the invocation...
 *         }
 *     }
 * }
 * </pre>
 * </p>
 *
 * <p>More information on how to apply interceptors can be found at the 
 * javadoc of the {@link Interceptor} interface.</p>
 *
 * {@beanlet.annotation}
 *
 * @see Interceptor
 * @see Interceptors
 * @author Leon van Zantvoort
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AroundInvoke {
    
}
