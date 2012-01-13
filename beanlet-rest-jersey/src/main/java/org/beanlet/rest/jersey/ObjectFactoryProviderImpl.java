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
package org.beanlet.rest.jersey;

import org.beanlet.BeanletWiringException;
import org.beanlet.common.AbstractProvider;
import org.beanlet.common.InjectantImpl;
import org.jargo.ComponentConfiguration;
import org.jargo.ComponentCreationException;
import org.jargo.ObjectFactory;
import org.jargo.ProxyGenerator;
import org.jargo.deploy.SequentialDeployable;
import org.jargo.spi.ObjectFactoryProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 *
 * @author Leon van Zantvoort
 */
public final class ObjectFactoryProviderImpl extends AbstractProvider 
        implements ObjectFactoryProvider {

    public Sequence sequence(SequentialDeployable deployable) {
        return SequentialDeployable.Sequence.BEFORE;
    }

    public <T> ObjectFactory<T> getObjectFactory(
            final ComponentConfiguration<T> configuration) {
        final ObjectFactory<T> factory;
        if (JerseyHelper.isJerseyObject(configuration.getComponentName())) {
            final Object restlet = JerseyHelper.getJerseyObject();
            assert restlet != null;
            factory = new ObjectFactory<T>() {
                @SuppressWarnings("unchecked")
                public T newInstance() {
                    return (T) restlet;
                }
                public T newInstance(ProxyGenerator<T> proxyGenerator) {
                    return proxyGenerator.generateProxy(new Class[0]);
                }
            };
        } else {
            factory = null;
        }
        return factory;
    }
}
