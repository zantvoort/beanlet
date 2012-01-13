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
package org.beanlet.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import org.beanlet.BeanletApplicationException;

/**
 * Add the following configuration to the web application's {@code web.xml} file
 * to support restlets.
 * </br>
 * Not required for Servlet 3.0 containers.
 * </br>
 * <pre>
 * &lt;web-app&gt;
 *   ...
 *   &lt;listener&gt;
 *     &lt;listener-class&gt;org.beanlet.rest.RequestContextListener&lt;listener-class&gt;
 *   &lt;/listener&gt;
 *   ...
 * &lt;/web-app&gt;
 * </pre>
 *
 * Restlets are deployed automatically by defining a RestFilter or RestServlet in the web.xml:
 *
 * <pre>
 * &lt;servlet&gt;
 * &lt;servlet-name&gt;Beanlet Rest Servlet&lt;/servlet-name&gt;
 * &lt;servlet-class&gt;org.beanlet.rest.RestServlet&lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;om.sun.jersey.config.property.packages&lt;/param-name&gt;
 *     &lt;param-value&gt;com.acme&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 * &lt;/servlet&gt;
 *
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;Beanlet Rest Servlet&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;  
 * </pre>
 *
 * @author Leon van Zantvoort
 */
public class RestServlet extends HttpServlet {

    private static class LazyHolder {
        static final Constructor<HttpServlet> delegate;
        static {
            try {
                try {
                    Constructor constructor = AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Constructor>() {
                                public Constructor run() throws Exception {
                                    String path = "META-INF/services/" +
                                            RestServlet.class.getName();

                                    // PERMISSION: java.lang.RuntimePermission getClassLoader
                                    ClassLoader loader = Thread.currentThread().
                                            getContextClassLoader();
                                    final Enumeration<URL> urls;
                                    if (loader == null) {
                                        urls = RequestContextListener.class.
                                                getClassLoader().getResources(path);
                                    } else {
                                        urls = loader.getResources(path);
                                    }
                                    while (urls.hasMoreElements()) {
                                        URL url = urls.nextElement();
                                        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                                url.openStream()));
                                        try {
                                            String className = null;
                                            while ((className = reader.readLine()) != null) {
                                                final String name = className.trim();
                                                if (!name.startsWith("#") && !name.startsWith(";") &&
                                                        !name.startsWith("//")) {
                                                    final Class<?> cls;
                                                    if (loader == null) {
                                                        cls = Class.forName(name);
                                                    } else {
                                                        cls = Class.forName(name, true, loader);
                                                    }
                                                    int m = cls.getModifiers();
                                                    if (HttpServlet.class.isAssignableFrom(cls) &&
                                                            !Modifier.isAbstract(m) &&
                                                            !Modifier.isInterface(m)) {
                                                        // PERMISSION: java.lang.RuntimePermission accessDeclaredMembers
                                                        Constructor constructor = cls.getDeclaredConstructor();
                                                        // PERMISSION: java.lang.reflect.ReflectPermission suppressAccessChecks
                                                        if (!Modifier.isPublic(constructor.getModifiers())) {
                                                            constructor.setAccessible(true);
                                                        }
                                                        return constructor;
                                                    } else {
                                                        throw new ClassCastException(cls.getName());
                                                    }
                                                }
                                            }
                                        } finally {
                                            reader.close();
                                        }
                                    }
                                    throw new BeanletApplicationException("No " +
                                            "RestServlet implementation " +
                                            "found.");
                                }
                            });
                    @SuppressWarnings("unchecked")
                    Constructor<HttpServlet> tmp =
                            (Constructor<HttpServlet>) constructor;
                    delegate = tmp;
                } catch (PrivilegedActionException e) {
                    throw e.getException();
                }
            } catch (BeanletApplicationException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new BeanletApplicationException(e);
            } catch (Error e) {
                throw e;
            } catch (Throwable t) {
                throw new BeanletApplicationException(t);
            }
        }
    }

    private final HttpServlet delegate;

    public RestServlet() {
        try {
            try {
                delegate = LazyHolder.delegate.newInstance();
            } catch (ExceptionInInitializerError e) {
                try {
                    throw e.getException();
                } catch (Throwable t) {
                    throw new BeanletApplicationException(t);
                }
            }
        } catch (BeanletApplicationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Exception e) {
            throw new BeanletApplicationException(e);
        }
    }

    @Override
    public String getServletName() {
        return delegate.getServletName();    
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        delegate.service(servletRequest, servletResponse);    
    }

    @Override
    public void destroy() {
        delegate.destroy();    
    }

    @Override
    public String getInitParameter(String s) {
        return delegate.getInitParameter(s);    
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return delegate.getInitParameterNames();    
    }

    @Override
    public ServletConfig getServletConfig() {
        return delegate.getServletConfig();    
    }

    @Override
    public ServletContext getServletContext() {
        return delegate.getServletContext();    
    }

    @Override
    public String getServletInfo() {
        return delegate.getServletInfo();    
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        delegate.init(servletConfig);    
    }

    @Override
    public void init() throws ServletException {
        delegate.init();    
    }

    @Override
    public void log(String s) {
        delegate.log(s);    
    }

    @Override
    public void log(String s, Throwable throwable) {
        delegate.log(s, throwable);    
    }
}
