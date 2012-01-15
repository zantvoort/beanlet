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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

/**
 * <p>Bootstrap class for the application container. An instance of the
 * {@code BeanletApplicationContext} can be obtained through the static 
 * {@code instance} method. If the container isn't already running, the
 * first call to this method automatically starts and initializes the container.
 * </p>
 * 
 * <p>During initialization, the container registers the beanlets listed in all 
 * the {@code beanlet.xml} and {@code META-INF/beanlet.xml} files that are 
 * available from the container's classpath.</p>
 * 
 * <p>The beanlet application context providess access to all registered 
 * beanlets. All methods of this class are thread-safe. They can be called at 
 * all time. That includes during initialization of the application container.
 * </p>
 * 
 * @author Leon van Zantvoort
 */
public abstract class BeanletApplicationContext {
    
    /**
     * The LazyHolder class is responsible for looking up the Beanlet container
     * implementation using Jar service discovery. Furthermore, this static 
     * class provides a solution for the flawed double-checked locking idiom, 
     */
    private static class LazyHolder {
        static final BeanletApplicationContext ctx;
        static {
            try {
                try {
                    Constructor constructor = AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Constructor>() {
                        public Constructor run() throws Exception {
                            String path = "META-INF/services/" + 
                                    BeanletApplicationContext.class.getName();

                            // PERMISSION: java.lang.RuntimePermission getClassLoader
                            ClassLoader loader = Thread.currentThread().
                                    getContextClassLoader();
                            final Enumeration<URL> urls;
                            if (loader == null) {
                                urls = BeanletApplicationContext.class.
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
                                            if (BeanletApplicationContext.class.isAssignableFrom(cls) &&
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
                                    "BeanletApplicationContext implementation " +
                                    "found.");
                        }
                    });
                    ctx = (BeanletApplicationContext) constructor.newInstance();
                } catch (PrivilegedActionException e) {
                    throw e.getException();
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            } catch (BeanletApplicationException e) {
                throw e;
            } catch (Error e) {
                throw e;
            } catch (Throwable t) {
                throw new BeanletApplicationException(t);
            }
        }
    }
  
    /**
     * <p>Returns a {@code BeanletApplicationContext} instance. If the container 
     * isn't already running, the first call to this method automatically starts 
     * and initializes the container.</p>
     * 
     * <p>It is not specified whether multiple calls to this method results in
     * a single or multiple instances of the {@code BeanletApplicationContext}. 
     * However, as the container only starts once, invoking this method multiple
     * times is valid and does not consume any additional resources.</p>
     * 
     * @return a {@code BeanletApplicationContext} instance.
     * @throws BeanletApplicationException indicates an error during container
     * initialization.
     */
    public static BeanletApplicationContext instance() throws 
            BeanletApplicationException {
        try {
            try {
                return LazyHolder.ctx.resolveInstance();
            } catch (ExceptionInInitializerError e) {
                try {
                    throw e.getException();
                } catch (BeanletApplicationException e2) {
                    throw e2;
                } catch (Error e2) {
                    throw e2;
                } catch (Throwable t) {
                    throw new BeanletApplicationException(t);
                }
            }
        } catch (BeanletApplicationException e) {   // Don't catch Errors and RuntimeExceptions.
            throw e;
        }
    }
    
    /**
     * Subclasses of this class can implement this method to control which 
     * instance is returned to the caller of the static {@code instance} method.
     * The default implementation of this method simply returns {@code this}.
     * 
     * @return a beanlet application context reference.
     * @throws BeanletApplicationContext indicates an error during container
     * initialization.
     */
    protected BeanletApplicationContext resolveInstance() throws 
            BeanletApplicationException {
        return this;
    }

    /**
     * Undeploys all components and stops all internal container threads.
     * @throws BeanletApplicationContext indicates an error during container
     * shutdown.
     */
    public abstract void shutdown() throws BeanletApplicationException;

    /**
     * Factory method for the specified {@code eventType}.
     *
     * @return a concrete implementation of the specified {@code eventType}.
     */
    public abstract <T extends Event> T getEvent(Class<T> eventType);
    
    /**
     * <p>Returns a beanlet instance for the specified {@code beanletName}.</p>
     *
     * <p>If his beanlet instance implements the {@code FactoryBeanlet} 
     * interface, the result of {@link FactoryBeanlet#getObject} is returned. 
     * Prefix the {@code beanletName} with {@code "&"} to obtain an instance to 
     * the {@code FactoryBeanlet} itself.</p>
     *
     * @param beanletName name of the beanlet.
     * @return a beanlet.
     * @throws BeanletNotFoundException if beanlet does not exist.
     * @throws BeanletCreationException if beanlet could not be created for any 
     * reason.
     */
    public abstract Object getBeanlet(String beanletName) throws 
            BeanletNotFoundException, BeanletCreationException;
    
    /**
     * <p>Returns a beanlet instance for the specified {@code beanletName}. If 
     * no beanlet exists for the specied {@code beanletName} a 
     * {@code BeanletNotFoundException} is thrown. A 
     * {@code BeanletNotOfRequiredTypeException} is thrown if the beanlet 
     * instance cannot be assigned to the {@code requiredType}.</p>
     * 
     * <p>If his beanlet instance implements the {@code FactoryBeanlet} 
     * interface, the result of {@link FactoryBeanlet#getObject} is returned. 
     * Prefix the {@code beanletName} with {@code "&"} to obtain an instance to 
     * the {@code FactoryBeanlet} itself.</p>
     * 
     * <p>The entries of the {@code info} argument can be used to wire members 
     * of the beanlet instance. This does not apply to singleton and stateless 
     * beanlets.</p>
     *
     * @param beanletName name of the beanlet.
     * @param requiredType type to mached the beanlet type.
     * @return a beanlet.
     * @throws BeanletNotFoundException if beanlet does not exist.
     * @throws BeanletCreationException if beanlet could not be created for
     * any reason.
     * @throws BeanletNotOfRequiredTypeException if beanlet cannot be assigned 
     * to the {@code requiredType}.
     * @see Inject
     * @see Wiring
     */
    public abstract <T> T getBeanlet(String beanletName, Class<T> requiredType) 
            throws BeanletNotFoundException, BeanletCreationException,
            BeanletNotOfRequiredTypeException;
    
    /**
     * <p>Returns a beanlet for the specified {@code beanletName}.</p>
     *
     * <p>If this beanlet implements the {@code FactoryBeanlet} 
     * interface, the result of {@link FactoryBeanlet#getObject} is returned. 
     * Prefix the {@code beanletName} with {@code "&"} to obtain an instance to 
     * the {@code FactoryBeanlet} itself.</p>
     * 
     * <p>The entries of the {@code info} argument can be used to wire members 
     * of the beanlet instance. This does not apply to static beanlets.</p>
     *
     * @param beanletName name of the beanlet.
     * @param info map that contains parameters that can be injected into
     * the beanlet instance.
     * @return a beanlet.
     * @throws BeanletNotFoundException if beanlet does not exist.
     * @throws BeanletCreationException if beanlet could not be 
     * created for any reason.
     * @see Inject
     * @see Wiring
     */
    public abstract Object getBeanlet(String beanletName,
            Map<String, ?> info) throws BeanletNotFoundException, 
            BeanletCreationException;
    
    /**
     * <p>Returns a {@code BeanletFactory} for the specified {@code beanletName}. 
     * If no beanlet exists for the specified {@code beanletName} a 
     * {@code BeanletNotFoundException} is thrown. If the beanlet definition
     * type is either the same as, or a subclass of the specified 
     * {@code requiredType}, a generified {@code BeanletFactory} is returned 
     * with the {@code requiredType} as upper bound.</p>
     *
     * <p>This method ignores the {@code "&"} prefix.</p>
     *
     * <p>The entries of the {@code info} argument can be used to wire members 
     * of the beanlet instance. This does not apply to static beanlets.</p>
     * 
     * @param beanletName name of the beanlet.
     * @param requiredType type to mached the beanlet type.
     * @param info map that contains parameters that can be injected into
     * the beanlet instance.
     * @return a {@code BeanletFactory} for the specified {@code beanletName}.
     * @throws BeanletNotFoundException if beanlet does not exist.
     * @throws BeanletCreationException if beanlet could not be  created for any 
     * reason.
     * @throws BeanletNotOfRequiredTypeException if beanlet cannot be assigned 
     * to the {@code requiredType}.
     * @see Inject
     * @see Wiring
     */
    public abstract <T> T getBeanlet(String beanletName, Class<T> requiredType,
            Map<String, ?> info) throws BeanletNotFoundException, 
            BeanletCreationException, BeanletNotOfRequiredTypeException;
    
    /**
     * Returns an immutable set of beanlet names of all registered beanlets.
     */
    public abstract Set<String> getBeanletNames();
    
    /**
     * Returns an immutable set of beanlet names of all registered beanlets, 
     * which the beanlet instance type is the same as, or a subclass of the
     * specified {@code type}.
     *
     * @param type type to mached the beanlet type.
     */
    public abstract Set<String> getBeanletNamesForType(Class<?> type);
    
    /**
     * Returns an immutable set of beanlet names of all registered beanlets, 
     * which the beanlet instance type is the same as, or a subclass of the
     * specified {@code type}. Additionally, beanlet factories, which return 
     * type match the specified {@code type} can be added as well, if 
     * {@code factoryAware} is set to {@code true}. Set {@code usePrefix} to 
     * {@code true} if these beanlet names must be prepended with "&".
     *
     * @param type type to mached the beanlet type, or factory 
     * beanlet return type (optional).
     * @param factoryAware specify {@code true} to include factory beanlets, 
     * which return type match the given type.
     * @param usePrefix specify {@code true} to prefix factory beanlet names.
     */
    public abstract Set<String> getBeanletNamesForType(Class<?> type, 
            boolean factoryAware, boolean usePrefix);
    
    /**
     * <p>Returns a {@code BeanletFactory} for the specified {@code beanletName},
     * or throws a {@code BeanletNotFoundException} if beanlet does not exist.</p>
     *
     * <p>This method ignores the {@code "&"} prefix.</p>
     *
     * @param beanletName name of the beanlet. 
     * @return a {@code BeanletFactory} for the specified {@code beanletName}.
     * @throws BeanletNotFoundException if beanlet does not exist.
     */
    public abstract BeanletFactory<?> getBeanletFactory(String beanletName) 
            throws BeanletNotFoundException;

    /**
     * <p>Returns a {@code BeanletFactory} for the specified {@code beanletName}. 
     * If no beanlet exists for the specified {@code beanletName} a 
     * {@code BeanletNotFoundException} is thrown. If the beanlet definition
     * type is either the same as, or a subclass of the specified 
     * {@code requiredType}, a generified {@code BeanletFactory} is returned 
     * with the {@code requiredType} as upper bound.</p>
     *
     * <p>This method ignores the {@code "&"} prefix.</p>
     *
     * @param beanletName name of the beanlet. 
     * @param requiredType type to mached the beanlet type.
     * @return a {@code BeanletFactory} for the specified {@code beanletName}.
     * @throws BeanletNotFoundException if beanlet does not exist.
     * @throws BeanletNotOfRequiredTypeException if beanlet's type
     * is not the same as, or a subtype of {@code requiredType}.
     */
    public abstract <T> BeanletFactory<? extends T> getBeanletFactory(
            String beanletName, Class<T> requiredType) throws 
            BeanletNotFoundException, BeanletNotOfRequiredTypeException;
    
    /**
     * <p>Returns {@code true} if a beanlet exists for the specified
     * {@code beanletName}.</p>
     *
     * <p>This method ignores the {@code "&"} prefix.</p>
     *
     * @param beanletName name of the beanlet.
     * @return {@code true} if beanlet exists, or {@code false} otherwise.
     */
    public abstract boolean exists(String beanletName);
}

