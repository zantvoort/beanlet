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
package org.beanlet.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import org.beanlet.common.AbstractProvider;
import org.jargo.ObjectFactory;
import org.jargo.ProxyGenerator;
import org.jargo.spi.ObjectFactoryProvider;
import org.jargo.ComponentConfiguration;
import org.jargo.ComponentCreationException;

/**
 *
 * @author Leon van Zantvoort
 */
public final class ObjectFactoryProviderImpl extends AbstractProvider 
        implements ObjectFactoryProvider {
    
    public <T> ObjectFactory<T> getObjectFactory(
            final ComponentConfiguration<T> configuration) {
        return new ObjectFactory<T>() {
            public T newInstance() {
                try {
                    final Class<T> cls = configuration.getType();
                    Constructor<T> constructor = AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Constructor<T>>() {
                        public Constructor<T> run() throws Exception {
                            // PERMISSION: java.lang.RuntimePermission accessDeclaredMembers
                            Constructor<T> constructor = cls.getDeclaredConstructor();
                            if (!Modifier.isPublic(constructor.getModifiers())) {
                                // PERMISSION: java.lang.reflect.ReflectPermission suppressAccessChecks
                                constructor.setAccessible(true);
                            }
                            return constructor;
                        }
                    });
                    try {
                        return constructor.newInstance();
                    } catch (InvocationTargetException e) {
                        throw e.getTargetException();
                    }
                } catch (ComponentCreationException e) {
                    throw e;
                } catch (PrivilegedActionException e) {
                    throw new ComponentCreationException(
                            configuration.getComponentName(), e.getException());
                } catch (Error e) {
                    throw e;
                } catch (Throwable t) {
                    // Errors are excluded.
                    throw new ComponentCreationException(
                            configuration.getComponentName(), t);
                }
            }
            public T newInstance(ProxyGenerator<T> proxyGenerator) {
                return proxyGenerator.generateProxy(new Class[0]);
            }
        };
    }
}
