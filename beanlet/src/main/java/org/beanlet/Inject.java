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
 * <p>Members marked with this annotation are injected by the application 
 * container during beanlet instance creation.</p>
 * 
 * <p>Dependency injection is supported for all member types - constructors, 
 * methods, and fields. Constructor dependency injection is used for 
 * injecting one or more objects into the constructor of a beanlet, resulting in 
 * a new beanlet instance. Alternatively, beanlet instances can be created by a 
 * (static) factory method. This factory method may defined by any class.
 * Only one constructor or factory method may be marked with this annotation to
 * avoid ambiguoutiy.
 * </p>
 *
 * <p>Setter dependency injection is applied to all fields and (non-static) 
 * methods that are marked with this annotation.</p>
 * 
 * <p>Dependency injection is performed before any lifecycle methods are invoked
 * on the beanlet instance. All members that are marked for (non-optional) 
 * dependency injection are guaranteed to be injected, otherwise beanlet 
 * instance creation fails.</p>
 * 
 * <p><h3>Injectant Resolution</h3>
 * <p>
 * This section explains the process of finding the object to be injected - 
 * further referred to as injectant - for a member that is target to dependency 
 * injection. The following steps are performed by the application container 
 * in listed order.
 * </p>
 * <p>
 * <b>(1)</b> First step is explicit dependency injection. The {@code Inject}
 * annotation specifies a value to be injected through the 
 * {@link #value}, {@link #collection} or {@link #map} methods.
 * </p>
 * <p>
 * <b>(2)</b> The container performs step two of injectant resulution if and 
 * only if step one does not result in an injectant. During step two the 
 * container checks if the member to be injected expresses a framework class.
 * These classes include: {@link BeanletApplicationContext}, 
 * {@link BeanletContext}, {@link BeanletMetaData}, {@link BeanletFactory} and 
 * {@link BeanletReference}. If the target matches 
 * {@code BeanletApplicationContext} the instance returned
 * by {@code BeanletApplicationContext.instance()} is injected. If the target
 * matches {@code BeanletContext} or {@code BeanletMetaData} the context or meta 
 * data of the underlying beanlet is injected. Note that it is allowed to 
 * parameterize the target is the parameterized type is assignable from this 
 * beanlet.<br>
 * If the target is assignable from {@code BeanletFactory} or
 * {@code BeanletReference} the container will lookup the factory or reference
 * of the beanlet referred to by the target. This process is described by the
 * next paragraph - Beanlet Name Resolution. Once again, the target is allowed 
 * to be parameterized as long as the parameterized type can be assigned from 
 * the referred beanlet.
 * </p>
 * <p>
 * <b>(3)</b> The final step of injectant resolution, named implicit wiring, is 
 * only executed if the {@link Wiring} annotation is applied with the 
 * appropiate wiring modes.</br>
 * <br>
 * There are three flavors of implicit wiring; 
 * {@code BY_INFO}, {@code BY_NAME} and {@code BY_TYPE}.<br>
 * The {@code BY_INFO} mode is used to allow injection through the info map
 * {@code BeanletFactory.create(Map)}, 
 * {@code BeanletApplicationContext.create(String, Map)}. If the name inferred 
 * from the target is contained in the info map its value is injected.<br>
 * For the {@code BY_NAME} mode, the container looks up the beanlet, which name 
 * is inferred from the target.<br>
 * The final wiring mode is wiring {@code BY_TYPE}. This mode looks up a beanlet
 * that matches the target's type. Injection only succeeds if exactly one 
 * beanlet is compliant to the target, it fails if either none or multiple 
 * beanlets are found.
 * </p>
 * <p>
 * Finally, if the three previously described steps did not result in an 
 * injection, a {@link BeanletWiringException} is thrown by the application 
 * container, unless the {@link #optional} method returns {@code true}.
 * </p>
 * 
 * <p><h3>Beanlet Name Resolution</h3>
 * The name of the beanlet is determined as follows:<br>
 * <br>
 * First, the beanlet name is extracted from the member that is to be injected.
 * If the member is a field, the name of the field declaration is used.<br>
 * If the member is a (setter) method, the beanlet name is derived from the 
 * method name. The beanlet name is a substring of the method name, starting at 
 * fourth character of the method name, cutting of {@code "set"}. The first 
 * character of this substring is converted to lowercase.<br>
 * Note that it is not possible to infer a beanlet name from a constructor.<br>
 * <br>
 * Next, the beanlet name - previously derived from the member - is overridden 
 * if {@link #name} is set to a value other than {@code ""}.<br>
 *
 * <p><h3>Examples</h3>
 * In case of the following three example classes and {@code beanlet.xml}
 * configuration file, beanlet {@code "bar"} is injected into field {@code foo}. 
 * As a result, beanlet {@literal "example"} has a dependency on beanlet 
 * {@literal "bar"}.<br>
 * <br>
 * <b>(A)</b> Example of Constructor Dependency Injection:
 * <pre>
 * &#64;Wiring(BY_NAME)
 * public class Example {
 *     
 *     private final Object foo;
 *
 *     &#64;Inject(name="foo")
 *     public Example(Object foo) {
 *         this.foo = foo;
 *     }
 *
 *     public void someBusinessMethod() {
 *     }
 * }
 * </pre>
 *
 * <b>(B)</b> Example of Setter Dependency Injection:
 * <pre>
 * &#64;Wiring(BY_NAME)
 * public class Example {
 *     
 *     private Object foo;
 *
 *     &#64;Inject 
 *     public void setFoo(Object foo) {
 *         this.foo = foo;
 *     }
 *
 *     public void someBusinessMethod() {
 *     }
 * }
 * </pre>
 *
 * <b>(C)</b> Example of Field Dependency Injection:
 * <pre>
 * &#64;Wiring(BY_NAME)
 * public class Example {
 *     
 *     &#64;Inject 
 *     private Object foo;
 *
 *     public void someBusinessMethod() {
 *     }
 * }
 * </pre>
 *
 * <p><h3>XML Representation</h3>The following xml-fragment shows how to express this annotation in xml.<br><pre><tt>&lt;beanlets xmlns="http://beanlet.org/schema/beanlet"
 *          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *          xsi:schemaLocation="http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd"&gt;
 *  &lt;beanlet name="foo" type="com.acme.Foo"&gt;
 *    <b>&lt;inject <i>field="bar"</i> name="" optional="false" 
 *            value="" type="java.lang.Object" ref="" 
 *            nill="false"/&gt;</b>
 *  &lt;/beanlet&gt;
 *&lt;/beanlets&gt;
 * 
 *&lt;beanlets xmlns="http://beanlet.org/schema/beanlet"
 *          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *          xsi:schemaLocation="http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd"&gt;
 *  &lt;beanlet name="foo" type="com.acme.Foo"&gt;
 *    <b>&lt;inject <i>field="bar"</i>&gt;
 *      &lt;value value="" type="java.lang.Object" ref="" 
 *             nill="false"/&gt;
 *    &lt;/inject&gt;</b>
 *  &lt;/beanlet&gt;
 *&lt;/beanlets&gt;
 *
 *&lt;beanlets xmlns="http://beanlet.org/schema/beanlet"
 *          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *          xsi:schemaLocation="http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd"&gt;
 *  &lt;beanlet name="foo" type="com.acme.Foo"&gt;
 *    <b>&lt;inject <i>field="bar"</i>&gt;
 *      &lt;collection type="java.util.ArrayList" synced="false" unmodifiable="false"&gt;
 *        &lt;value value="" type="java.lang.Object" ref="" 
 *               nill="false"/&gt;
 *      &lt;/collection&gt;
 *    &lt;/inject&gt;</b>
 *  &lt;/beanlet&gt;
 *&lt;/beanlets&gt;
 *
 *&lt;beanlets xmlns="http://beanlet.org/schema/beanlet"
 *          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *          xsi:schemaLocation="http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd"&gt;
 *  &lt;beanlet name="foo" type="com.acme.Foo"&gt;
 *    <b>&lt;inject <i>field="bar"</i>&gt;
 *      &lt;map type="java.util.HashMap" synced="false" unmodifiable="false"&gt;
 *        &lt;entry&gt;
 *          &lt;key value="" type="java.lang.Object" ref="" 
 *               nill="false"/&gt;
 *          &lt;value value="" type="java.lang.Object" ref="" 
 *                 nill="false"/&gt;
 *        &lt;/entry&gt;
 *        &lt;entry key="" value=""/&gt;
 *      &lt;/map&gt;
 *    &lt;/inject&gt;</b>
 *  &lt;/beanlet&gt;
 *&lt;/beanlets&gt;
 *
 *&lt;beanlets xmlns="http://beanlet.org/schema/beanlet"
 *          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *          xsi:schemaLocation="http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd"&gt;
 *  &lt;beanlet name="foo" type="com.acme.Foo"&gt;
 *    <b>&lt;inject <i>field="bar"</i>&gt;
 *      &lt;beanlet name="innerFoo" type="com.acme.InnerFoo"/&gt;
 *    &lt;/inject&gt;</b>
 *  &lt;/beanlet&gt;
 *&lt;/beanlets&gt;</tt></pre></p>
 *
 * @see Wiring
 * @author Leon van Zantvoort
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, 
        ElementType.PARAMETER})
public @interface Inject {
    
    /**
     * {@code true} if injection is optional, {@code false} otherwise.
     */
    boolean optional() default false;
 
    /**
     * Overrides the name inferred from the marked member, unless it returns
     * an empty string (""). How this name is used depends on the selected type 
     * of wiring.
     */
    String name() default "";
    
    /**
     * Overrides the type inferred from the marked member, unless it returns
     * the Object class (Object.class). How this type is used depends on the 
     * selected type of wiring.
     */
    Class<?> type() default Object.class;
    
    /**
     * Returns the name of the beanlet to be injected. 
     * 
     * <p>Shortcut for {@code &#64;Inject(&#64;Value(ref=""))}</p>
     */
    String ref() default "";

    /**
     * Used to inject a value as specified by this {@code Value} annotation.
     */
    Value value() default @Value;

    /**
     * Used to inject a {@code Collection} or array as specified by this
     * {@code CollectionValue} annotation.
     */
    CollectionValue collection() default @CollectionValue;
    
    /**
     * Used to inject a {@code Map} as specified by this
     * {@code MapValue} annotation.
     */
    MapValue map() default @MapValue;
}
