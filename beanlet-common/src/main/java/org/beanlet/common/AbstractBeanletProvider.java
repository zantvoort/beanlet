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

import org.beanlet.plugin.BeanletConfiguration;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.beanlet.BeanletValidationException;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.annotation.MethodElement;
import org.jargo.ComponentConfiguration;

/**
 *
 * @author Leon van Zantvoort
 */
public abstract class AbstractBeanletProvider extends AbstractProvider {
    
    // Optimization
    private static final Object DUMMY = new Object();
    private final Map<ComponentConfiguration, Object> cache;
    
    public AbstractBeanletProvider() {
        // The WeakHashMap's key is not reference by the map's value.
        cache = Collections.synchronizedMap(
                new WeakHashMap<ComponentConfiguration, Object>());
    }
    
    // ANNOTATION
    
    /**
     * Specifies the annotation type that is used to identify the method.
     */
    public abstract Class<? extends Annotation> annotationType();
    
    // INTERFACE
    
    public abstract Method getInterfaceMethod();
    
    /**
     * Returns <tt>null</tt> if no method is found.
     */
    public Method getMethod(ComponentConfiguration<?> configuration) {
        Method method = null;
        Object o = cache.get(configuration);
        if (o == null) {
            if (configuration instanceof BeanletConfiguration) {
                Set<Method> methods = new LinkedHashSet<Method>();

                Class<?> cls = configuration.getType();

                // INTERFACE
                Method infMethod = getInterfaceMethod();
                if (infMethod != null) {
                    assert infMethod.getDeclaringClass().isInterface() : 
                            "Method not declared by interface: " + infMethod + ".";
                    if (infMethod.getDeclaringClass().isAssignableFrom(cls)) {
                        try {
                            methods.add(cls.getMethod(infMethod.getName(), 
                                    infMethod.getParameterTypes()));
                        } catch (NoSuchMethodException e) {
                            assert false;
                        }
                    }
                }

                // ANNOTATION
                AnnotationDomain domain = ((BeanletConfiguration) configuration).
                        getAnnotationDomain();
                for (ElementAnnotation<MethodElement, ? extends Annotation> ea : 
                        domain.getDeclaration(annotationType()).
                        getTypedElements(MethodElement.class, cls)) {
                    methods.add(ea.getElement().getMethod());
                }

                for (Method m : methods) {
                    if (method != null) {
                        throw new BeanletValidationException(configuration.getComponentName(),
                                "Multiple methods found for " + 
                                annotationType().getSimpleName() + 
                                " annotation: " +  methods + ".");
                    }
                    method = m;
                }
            }
            if (method == null) {
                cache.put(configuration, DUMMY);
            } else {
                cache.put(configuration, method);
            }
        } else {
            if (o != DUMMY) {
                method =(Method) o;
            }
        }
        return method;
    }
}
