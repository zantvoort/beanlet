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

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.beanlet.common.AbstractProvider;
import org.jargo.spi.ClassLoaderProvider;

/**
 * @author Leon van Zantvoort
 */
public final class ClassLoaderProviderImpl extends AbstractProvider implements 
        ClassLoaderProvider {

    public ClassLoader getClassLoader(final URL url) {
        return AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                // PERMISSION: java.lang.RuntimePermission getClassLoader
                ClassLoader parent = Thread.currentThread().getContextClassLoader();
                if (parent == null) {
                    // PERMISSION: java.lang.RuntimePermission createClassLoader
                    return new BeanletClassLoader(new URL[]{url});
                } else {
                    // PERMISSION: java.lang.RuntimePermission createClassLoader
                    return new BeanletClassLoader(new URL[]{url}, parent);
                }
            }
        });
    }
}
