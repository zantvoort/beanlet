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
package org.beanlet.persistence.impl;

import org.beanlet.persistence.spi.PersistencePropertiesProvider;
import org.jargo.deploy.Deployable;
import org.jargo.deploy.Deployer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Leon van Zantvoort
 */
public final class PersistencePropertiesFactory implements PersistencePropertiesProvider {

    private static final PersistencePropertiesFactory factory = new PersistencePropertiesFactory();

    public static PersistencePropertiesProvider getInstance() {
        return factory;
    }

    private final PersistencePropertiesProvider provider;

    private PersistencePropertiesFactory() {
        Logger logger = Logger.getLogger(getClass().getName());
        PersistencePropertiesProvider p = null;
        try {
            final ClassLoader loader = AccessController.doPrivileged(
                    new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    // PERMISSION: java.lang.RuntimePermission getClassLoader
                    return PersistencePropertiesFactory.class.getClassLoader();
                }
            });
            String path = "META-INF/services/" + PersistencePropertiesProvider.class.getName();
            Enumeration<URL> urls = getClass().getClassLoader().
                    getResources(path);
            Set<String> dupes = new HashSet<String>();
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                try {
                    String className = null;
                    while ((className = reader.readLine()) != null) {
                        final String name = className.trim();
                        if (name.length() != 0 &&
                                !name.startsWith("#") &&
                                !name.startsWith(";") &&
                                !name.startsWith("//")) {
                            try {
                                Constructor constructor = AccessController.
                                        doPrivileged(new PrivilegedExceptionAction<Constructor>() {
                                            public Constructor run() throws Exception {
                                                Class<?> cls = Class.forName(name, true, loader);
                                                int m = cls.getModifiers();
                                                if (Deployable.class.isAssignableFrom(cls) &&
                                                        !Modifier.isAbstract(m) &&
                                                        !Modifier.isInterface(m)) {
                                                    // PERMISSION: java.lang.RuntimePermission accessDeclaredMembers
                                                    Constructor constructor = cls.getDeclaredConstructor();
                                                    if (!Modifier.isPublic(constructor.getModifiers())) {
                                                        // PERMISSION: java.lang.reflect.ReflectPermission suppressAccessChecks
                                                        constructor.setAccessible(true);
                                                    }
                                                    return constructor;
                                                } else {
                                                    throw new ClassCastException(cls.getName());
                                                }
                                            }
                                        });
                                p = (PersistencePropertiesProvider) constructor.newInstance();
                                break;
                            } catch (PrivilegedActionException e) {
                                throw e.getException();
                            }
                        }
                    }
                } finally {
                    reader.close();
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unable to load PersistencePropertiesProvider.", e);
        }
        provider = p;
    }

    public Map<?, ?> getProperties() {
        return provider == null ? Collections.<Object, Object>emptyMap() :
                Collections.unmodifiableMap(provider.getProperties());
    }
}
