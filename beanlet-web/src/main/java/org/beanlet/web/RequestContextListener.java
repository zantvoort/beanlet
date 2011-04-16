package org.beanlet.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import org.beanlet.BeanletApplicationException;

/**
 * Add the following configuration to the web application's {@code web.xml} file
 * to support the {@code Request} and {@code Session} beanlet scopes.
 * 
 * <pre>
 * &lt;web-app&gt;
 *   ...
 *   &lt;listener&gt;
 *     &lt;listener-class&gt;org.beanlet.web.RequestContextListener&lt;listener-class&gt;
 *   &lt;/listener&gt;
 *   ...
 * &lt;/web-app&gt;
 * </pre>
 * 
 * @see Request
 * @see Session
 * @author Leon van Zantvoort
 */
public class RequestContextListener implements ServletRequestListener {

    private static class LazyHolder {
        static final Constructor<ServletRequestListener> delegate;
        static {
            try {
                try {
                    Constructor constructor = AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Constructor>() {
                        public Constructor run() throws Exception {
                            String path = "META-INF/services/" + 
                                    RequestContextListener.class.getName();

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
                    Constructor<ServletRequestListener> tmp = 
                            (Constructor<ServletRequestListener>) constructor;
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

    private final ServletRequestListener delegate;
    
    public RequestContextListener() {
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
    
    public void requestInitialized(ServletRequestEvent event) {
        delegate.requestInitialized(event);
    }

    public void requestDestroyed(ServletRequestEvent event) {
        delegate.requestDestroyed(event);
    }
}
