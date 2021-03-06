package org.beanlet.web;

import org.beanlet.BeanletApplicationException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;

/**
 * Add the following configuration to the web application's {@code web.xml} file
 * to support the {@code Request} and {@code Session} beanlet scopes.
 * </br>
 * Not required for Servlet 3.0 containers.
 * </br>
 * <pre>
 * &lt;web-app&gt;
 *   ...
 *   &lt;listener&gt;
 *     &lt;listener-class&gt;org.beanlet.web.ServletContextListener&lt;listener-class&gt;
 *   &lt;/listener&gt;
 *   ...
 * &lt;/web-app&gt;
 * </pre>
 * 
 * @see Request
 * @see Session
 * @author Leon van Zantvoort
 */
public class ServletContextListener implements javax.servlet.ServletContextListener {

    private static class LazyHolder {
        static final Constructor<javax.servlet.ServletContextListener> delegate;
        static {
            try {
                try {
                    Constructor constructor = AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Constructor>() {
                        public Constructor run() throws Exception {
                            String path = "META-INF/services/" + 
                                    ServletContextListener.class.getName();

                            // PERMISSION: java.lang.RuntimePermission getClassLoader
                            ClassLoader loader = Thread.currentThread().
                                    getContextClassLoader();
                            final Enumeration<URL> urls;
                            if (loader == null) {
                                urls = ServletContextListener.class.
                                        getClassLoader().getResources(path);
                            } else {
                                urls = loader.getResources(path);
                            }
                            while (urls.hasMoreElements()) {
                                URL url = urls.nextElement();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(
                                        url.openStream()));
                                try {
                                    String className;
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
                                            if (ServletRequestListener.class.isAssignableFrom(cls) &&
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
                                    "ServletRequestListener implementation " +
                                    "found.");
                        }
                    });
                    @SuppressWarnings("unchecked")
                    Constructor<javax.servlet.ServletContextListener> tmp =
                            (Constructor<javax.servlet.ServletContextListener>) constructor;
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

    private final javax.servlet.ServletContextListener delegate;
    
    public ServletContextListener() {
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

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        delegate.contextInitialized(servletContextEvent);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        delegate.contextDestroyed(servletContextEvent);
    }
}
