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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.beanlet.BeanletValidationException;
import org.beanlet.ProxyMethod;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.annotation.MethodElement;
import org.beanlet.plugin.BeanletConfiguration;
import org.jargo.ComponentConfiguration;

/**
 * @author Leon van Zantvoort
 */
public final class ProxyMethods {
    
    private static final Map<ComponentConfiguration, ProxyMethods> cache =
            new HashMap<ComponentConfiguration, ProxyMethods>();

    public static final ProxyMethods getInstance() {
        return getInstance(null);
    }
    
    public static final synchronized ProxyMethods getInstance(
            final ComponentConfiguration<?> configuration) {
        ProxyMethods proxyMethods = cache.get(configuration);
        if (proxyMethods == null) {
            if (configuration instanceof BeanletConfiguration) {
                proxyMethods = new ProxyMethods((BeanletConfiguration<?>) configuration);
            } else {
                proxyMethods = new ProxyMethods();
            }
            configuration.getComponentUnit().addDestroyHook(new Runnable() {
                public void run() {
                    synchronized (ProxyMethods.class) {
                        cache.remove(configuration);
                    }
                }
            });
            cache.put(configuration, proxyMethods);
        }
        return proxyMethods;
    }

    private final Method proxyMethod;
    
    private ProxyMethods() {
        this.proxyMethod = null;
    }
    
    private ProxyMethods(BeanletConfiguration configuration) {
        AnnotationDomain domain = configuration.getAnnotationDomain();
        
        Method tmpProxyMethod = null;
        Class<?> type = configuration.getType();
        
        for (ElementAnnotation<MethodElement, ProxyMethod> e : 
                domain.getDeclaration(ProxyMethod.class).
                getTypedElements(MethodElement.class, type)) {
            Method method = e.getElement().getMethod();
                
            if (method.getParameterTypes().length == 3 &&
                    method.getParameterTypes()[0].equals(String.class) &&
                    method.getParameterTypes()[1].equals(Class[].class) &&
                    method.getParameterTypes()[2].equals(Object[].class) &&
                    method.getReturnType().equals(Object.class)) {
                if (!e.getElement().isOverridden(type)) {
                    if (tmpProxyMethod != null) {
                        throw new BeanletValidationException(configuration.getComponentName(),
                                "Duplicate ProxyMethod methods found: " + 
                                Arrays.asList(tmpProxyMethod, method) + ".");
                    }
                    tmpProxyMethod = method;
                }
            } else {
                throw new BeanletValidationException(configuration.getComponentName(),
                        "Invalid signature for ProxyMethod method: '" + 
                        method + "'.");
            }
        }
        this.proxyMethod = tmpProxyMethod;
    }
    
    /**
     * Returns proxy method for this beanlet, or {@code null} if not available.
     */
    public Method getProxyMethod() {
        return proxyMethod;
    }
}
