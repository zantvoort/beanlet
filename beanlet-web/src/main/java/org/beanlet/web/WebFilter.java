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
package org.beanlet.web;

import org.beanlet.ScopeAnnotation;

import javax.servlet.DispatcherType;
import javax.servlet.annotation.WebInitParam;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation used to declare a Servlet {@link javax.servlet.Filter}.
 * This feature is only supported for Servlet API 3.0 and onwards.<br/>
 * <br />
 *
 * This annotation will be processed by the container during deployment, the
 * Filter class in which it is found will be created as per the configuration
 * and applied to the URL patterns, {@link javax.servlet.Servlet}s and
 * {@link javax.servlet.DispatcherType}s.<br />
 * <br/>
 *
 * If the name attribute is not defined, the fully qualified name of the class
 * is used.<br/>
 * <br/>
 *
 * At least one URL pattern MUST be declared in either the {@code value} or
 * {@code urlPattern} attribute of the annotation, but not both.<br/>
 * <br/>
 *
 * The {@code value} attribute is recommended for use when the URL pattern is
 * the only attribute being set, otherwise the {@code urlPattern} attribute
 * should be used.<br />
 * <br />
 *
 * The annotated class MUST implement {@link javax.servlet.Filter}.
 *
 * E.g.
 *
 * <code>@WebFilter("/path/*")</code><br />
 * <code>public class AnExampleFilter implements Filter { ... </code><br />
 *     
 *     
 * <p><h3>XML Representation</h3>The following xml-fragment shows how to express this annotation in xml.<br><pre><tt>&lt;beanlet type="com.acme.servlet.TestFilter"&gt;
 *    <b>&lt;web:filter filter-name="testFilter" create-filter="true"&gt;
 *        &lt;!--web:url-pattern value="/test/TestServlet"/--&gt;
 *        &lt;web:init-param key="test-user" value="john"/&gt;
 *        &lt;web:servlet-name value="TestServlet"/&gt;
 *    &lt;/web:filter&gt;</b>
 *&lt;/beanlet&gt;
 *
 *&lt;beanlet type="com.acme.servlet.TestFilter"&gt;
 *     <b>&lt;web:filter filter-name="testFilter"&gt;
 *          &lt;!--&lt;web:url-patterns&gt;--&gt;
 *              &lt;!--&lt;web:url-pattern value="/test/TestServlet"/&gt;--&gt;
 *              &lt;!--&lt;web:url-pattern value="/test/ProductionServlet"/&gt;--&gt;
 *          &lt;!--&lt;/web:url-patterns&gt;--&gt;
 *          &lt;web:init-params&gt;
 *              &lt;web:init-param key="test-user" value="john"/&gt;
 *              &lt;web:init-param key="production-user" value="john"/&gt;
 *          &lt;/web:init-params&gt;
 *          &lt;web:servlet-names&gt;
 *              &lt;web:servlet-name value="TestServlet"/&gt;
 *              &lt;web:servlet-name value="ProductionServlet"/&gt;
 *        &lt;/web:servlet-names&gt;
 *    &lt;/web:filter&gt;</b>
 *&lt;/beanlet&gt;</tt></pre>
 *
 * @author Leon van Zantvoort
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebFilter {

    /**
     * @return <code>true</code> if instance is created by Servlet container.
     */
    boolean createFilter() default true;

    /**
     * @return description of the Filter, if present
     */
    String description() default "";

    /**
     * @return display name of the Filter, if present
     */
    String displayName() default "";

    /**
     * @return array of initialization params for this Filter
     */
    WebInitParam[] initParams() default {};

    /**
     * @return name of the Filter, if present
     */
    String filterName() default "";

    /**
     * @return small icon for this Filter, if present
     */
    String smallIcon() default "";

    /**
     * @return the large icon for this Filter, if present
     */
    String largeIcon() default "";

    /**
     * @return array of Servlet names to which this Filter applies
     */
    String[] servletNames() default {};

    /**
     * A convenience method, to allow extremely simple annotation of a class.
     *
     * @return array of URL patterns
     * @see #urlPatterns()
     */
    String[] value() default {};

    /**
     * @return array of URL patterns to which this Filter applies
     */
    String[] urlPatterns() default {};

    /**
     * @return array of DispatcherTypes to which this filter applies
     */
    DispatcherType[] dispatcherTypes() default {DispatcherType.REQUEST};

    /**
     * @return asynchronous operation supported by this Filter
     */
    boolean asyncSupported() default false;
}
