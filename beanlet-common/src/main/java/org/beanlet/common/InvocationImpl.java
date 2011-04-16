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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import org.jargo.Invocation;

/**
 *
 * @author Leon van Zantvoort
 */
public class InvocationImpl implements Invocation {
    
    private static final Object[] EMPTY_ARRAY = new Object[0];
    
    private final Method method;
    private Object[] args;
    
    public InvocationImpl() {
        this(null);
    }
    
    public InvocationImpl(Method method, Object... args) {
        this.method = method;
        this.args = args == null ? EMPTY_ARRAY : args;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public void setParameters(Object[] args) {
        this.args = args == null ? EMPTY_ARRAY : args;
    }
    
    public Object[] getParameters() {
        return args;
    }
    
    public Object invoke(Object instance) throws Exception {
        return invoke(instance, getMethod(), getParameters());
    }
    
    public String toString() {
        return "Invocation{method=" + getMethod() + "}";
    }

    public static Object invoke(Object instance, final Method method, 
            Object... parameters) throws Exception {
        try {
            if (!Modifier.isPublic(method.getModifiers())) {
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        // PERMISSION: java.lang.reflect.ReflectPermission suppressAccessChecks
                        method.setAccessible(true);
                        return null;
                    }
                });
            }
            return method.invoke(instance, parameters);
        } catch (IllegalArgumentException e) {
            List<Class> classes = new ArrayList<Class>();
            for (Object o : parameters) {
                classes.add(o == null ? null : o.getClass());
            }
            throw new IllegalArgumentException(e.getMessage() + " " + 
                    method.getName() + 
                    (instance == null ? "" : " " + instance.getClass()) + 
                    " " + classes);
        } catch (InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (Exception e2) {
                throw e2;
            } catch (Error e2) {
                throw e2;
            } catch (Throwable t) {
                throw new AssertionError(t);
            }
        }
    }
}
