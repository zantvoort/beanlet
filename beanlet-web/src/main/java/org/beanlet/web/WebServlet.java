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

import javax.servlet.annotation.WebInitParam;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to declare the configuration of an
 * {@link javax.servlet.Servlet}. This feature is only supported for Servlet API 3.0
 * and onwards.<br/>
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
 * The class on which this annotation is declared MUST extend
 * {@link javax.servlet.http.HttpServlet}. <br />
 * <br />
 *
 * E.g. <code>@WebServlet("/path")}<br />
 * public class TestServlet extends HttpServlet ... {</code><br />
 *
 * E.g.
 * <code>@WebServlet(name="TestServlet", urlPatterns={"/path", "/alt"}) <br />
 * public class TestServlet extends HttpServlet ... {</code><br />
 *
 * <p><h3>XML Representation</h3>The following xml-fragment shows how to express this annotation in xml.<br><pre><tt>&lt;beanlet type="com.google.gwt.user.server.rpc.RemoteServiceServlet"&gt;
 *    <b>&lt;web:servlet name="TestServlet" create-servlet="false"&gt;
 *        &lt;web:url-pattern value="/test/TestServlet"/&gt;
 *        &lt;web:init-param key="test-user" value="john"/&gt;
 *    &lt;/web:servlet&gt;</b>
 *    &lt;inject constructor="true" index="0"&gt;
 *        &lt;beanlet type="com.acme.servlet.TestService"/&gt;
 *    &lt;/inject&gt;
 *&lt;/beanlet&gt;
 *
 *&lt;beanlet type="com.google.gwt.user.server.rpc.RemoteServiceServlet"&gt;
 *    <b>&lt;web:servlet name="TestServlet" create-servlet="true"&gt;
 *        &lt;web:url-patterns&gt;
 *            &lt;web:url-pattern value="/test/TestServlet"/&gt;
 *            &lt;web:url-pattern value="/test/ProductionServlet"/&gt;
 *        &lt;/web:url-patterns&gt;
 *        &lt;web:init-params&gt;
 *            &lt;web:init-param key="test-user" value="john"/&gt;
 *            &lt;web:init-param key="production-user" value="john"/&gt;
 *        &lt;/web:init-params&gt;
 *    &lt;/web:servlet&gt;</b>
 *&lt;/beanlet&gt;</tt></pre>
 *
 * @author Leon van Zantvoort
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebServlet {

    /**
     * @return <code>true</code> if instance is created by Servlet container.
     */
    boolean createServlet() default true;

    /**
     * @return name of the Servlet
     */
    String name() default "";

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
     * @return load on startup ordering hint
     */
    int loadOnStartup() default -1;

    /**
     * @return array of initialization params for this Servlet
     */
    WebInitParam[] initParams() default {};

    /**
     * @return asynchronous operation supported by this Servlet
     */
    boolean asyncSupported() default false;

    /**
     * @return small icon for this Servlet, if present
     */
    String smallIcon() default "";

    /**
     * @return large icon for this Servlet, if present
     */
    String largeIcon() default "";

    /**
     * @return description of this Servlet, if present
     */
    String description() default "";

    /**
     * @return display name of this Servlet, if present
     */
    String displayName() default "";
}
