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
package org.beanlet.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.jargo.ConstructorInjection;
import org.jargo.ProxyGenerator;

/**
 *
 * @author Leon van Zantvoort
 */
public class FactoryFieldInjectionImpl<T> implements ConstructorInjection<T> {
    
    private final Field field;
    
    public FactoryFieldInjectionImpl(Field field) {
        this.field = field;
    }
    
    public Field getField() {
        return field;
    }
    
    public String toString() {
        return "{field=" + getField().getName() + "}";
    }

    public Object inject() {
        final Field field = getField();
        if (!Modifier.isPublic(field.getModifiers())) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    // PERMISSION: java.lang.reflect.ReflectPermission suppressAccessChecks
                    field.setAccessible(true);
                    return null;
                }
            });
        }
        try {
            try {
                return field.get(null);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(String.valueOf(field) +
                        (e.getMessage() == null ? "" : (": " + e.getMessage())));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public T inject(ProxyGenerator<T> proxyGenerator) throws 
            UnsupportedOperationException {
        throw new UnsupportedOperationException("Proxy generation not " +
                "supported for objects retrieved from static factory field. " +
                "Factory field: '" + field + "'.");
    }
}
