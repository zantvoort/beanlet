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
 * <p>Declares a list of interceptors. The order in which the interceptors are 
 * executed is equal to the order in which they are specified by this 
 * annotation. Interceptors can be specified at both class-level and 
 * method-level. Class-level interceptors are applied to all business methods
 * of a beanlet, except for business methods that are marked with the 
 * {@link ExcludeClassInterceptors} annotation. Logically, method-level 
 * interceptors are only applied to the method at which they are specified.</p>
 * 
 * <p><h3>Interceptor Execution Order</h3>
 * Class-level interceptors are placed in front of method-level interceptors.
 * The interceptor chain does not allow the same interceptor class more than
 * once. The classes specified by {@link #value} MUST therefore not specify 
 * duplicate classes. If the class-level and method-level interceptor 
 * declaration share classes, the duplicate class-level interceptors are 
 * discarded. This allows the method-level annotation to override the order 
 * specified at class-level. This situation is demonstrated at example (A).
 * 
 * <p><h3>Interceptor Instance Scope</h3>
 * New interceptor instances are created for every beanlet instance that is 
 * constructed. These instances are always associated with this particular 
 * beanlet instance.
 * A new interceptor chain is assembled for every single interceptor-enabled 
 * method of this beanlet instance. This interceptor chain is composed of the
 * previously created interceptor instances. The applicability of an interceptor 
 * to more than one business method of a beanlet does not affect the 
 * relationship between the interceptor instance and the beanlet instance. Only
 * a single instance of the interceptor class is created per beanlet instance.
 * </p>
 * 
 * <p><h3>Examples</h3>
 * <b>(A)</b> Example of a beanlet with duplicate interceptor 
 * ({@code InterceptorB}) declarations. The order in which these interceptors
 * are executed for method {@code sum(int[])} is:
 * {@code InterceptorA}, {@code InterceptorC}, {@code InterceptorB} and finally 
 * {@code InterceptorD}.
 * The order for {@code subtract(int, int)} is: {@code InterceptorB} 
 * and {@code InterceptorA}. Class-level interceptor {@code InterceptorC} is not
 * installed for this method, because the {@code ExcludeClassInterceptors} 
 * annotation excludes class-level interceptors.
 * 
 * <pre>
 * &#64;Interceptors({InterceptorA.class, InterceptorB.class, InterceptorC.class})
 * public class ExampleBeanlet {
 *     
 *     &#64;Interceptors({InterceptorB.class, InterceptorD.class})
 *     &#64;Operation 
 *     public int add(int... values) {
 *         int sum = 0;
 *         for (int value : values) {
 *             sum += value;
 *         }
 *         return sum;
 *     }
 * 
 *     &#64;ExcludeClassInterceptors
 *     &#64;Interceptors({InterceptorB.class, InterceptorA.class})
 *     &#64;Operation 
 *     public int subtract(int arg0, int arg1) {
 *         return arg0 - arg1;
 *     }
 * }
 * </pre>
 * 
 * <b>(B)</b> Interceptor instance scope example. Only two interceptor
 * instances are created for an instance of beanlet {@code "example"}, namely
 * an instance of {@code InterceptorA} and an instance of {@code InterceptorB}.
 * Hence, method {@code doIt()}, {@code doThis(String)} and 
 * {@code doThat(String)} share an instance of {@code InterceptorA}. 
 * {@code InterceptorB} is shared by {@code doThis(String)} and 
 * {@code doThat(String)}.
 * 
 * <pre>
 * &#64;Interceptors({InterceptorA.class})
 * public class ExampleBeanlet {
 *     
 *     &#64;Run 
 *     public void doIt() {
 *     }
 * 
 *     &#64;Interceptors(InterceptorB.class)
 *     &#64;Operation 
 *     public void doThis(String this) {
 *     }
 * 
 *     &#64;Interceptors(InterceptorB.class)
 *     &#64;Operation 
 *     public void doThat(String that) {
 *     }
 * }
 * </pre>
 * </p>
 * 
 * <p><h3>XML Representation</h3>The following xml-fragment shows how to express this annotation in xml.<br><pre><tt>&lt;beanlets xmlns="http://beanlet.org/schema/beanlet"
 *          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *          xsi:schemaLocation="http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd"&gt;
 *  &lt;beanlet name="foo" type="com.acme.Foo"&gt;
 *    <b>&lt;interceptors type="com.acme.InterceptorA"/&gt;</b>
 *  &lt;/beanlet&gt;
 *&lt;/beanlets&gt;</tt></pre></p>
 * Or alternatively:
 * <p><pre><tt>&lt;beanlets xmlns="http://beanlet.org/schema/beanlet"
 *          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *          xsi:schemaLocation="http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd"&gt;
 *  &lt;beanlet name="foo" type="com.acme.Foo"&gt;
 *    <b>&lt;interceptors&gt;
 *      &lt;class type="com.acme.InterceptorA"/&gt;
 *      &lt;class type="com.acme.InterceptorB"/&gt;
 *    &lt;/interceptors&gt;</b>
 *  &lt;/beanlet&gt;
 *&lt;/beanlets&gt;</tt></pre></p>
 * @author Leon van Zantvoort
 * @see AroundInvoke
 * @see Interceptor
 * @see ExcludeClassInterceptors
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Interceptors {
    
    /**
     * Specifies interceptor classes. These classes are executed in the order
     * they are specified.
     */
    Class<?>[] value();
}
