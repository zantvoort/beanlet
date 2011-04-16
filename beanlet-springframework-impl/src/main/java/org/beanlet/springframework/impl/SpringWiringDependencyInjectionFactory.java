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
package org.beanlet.springframework.impl;

import java.lang.reflect.Method;
import static org.beanlet.WiringMode.*;
import static org.beanlet.springframework.impl.SpringHelper.*;
import org.beanlet.Inject;
import org.beanlet.Wiring;
import org.beanlet.WiringMode;
import org.beanlet.annotation.AnnotationProxy;
import org.beanlet.annotation.AnnotationValueResolver;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.common.WiringDependencyInjectionFactory;
import org.beanlet.springframework.SpringContext;

/**
 *
 * @author Leon van Zantvoort
 */
public abstract class SpringWiringDependencyInjectionFactory extends
        WiringDependencyInjectionFactory {
    
    private final BeanletConfiguration<?> configuration;
    
    public SpringWiringDependencyInjectionFactory(
            BeanletConfiguration<?> configuration) {
        super(configuration);
        this.configuration = configuration;
    }
    
    /**
     * Returns {@code true} if specified element supports the wiring method
     * expressed by this factory.
     */
    @Override
    public boolean isWiringModeSupported(
            ElementAnnotation<? extends Element, Inject> ea) {
        final boolean supported;
        SpringContext namingContext = getSpringContext(configuration, 
                ea.getElement());
        if (namingContext != null) {
            final Wiring defaultWiring;
            if (getSpringContext(configuration, ea.getElement(), true) == null) {
                defaultWiring = AnnotationProxy.newProxyInstance(Wiring.class);
            } else {
                defaultWiring = AnnotationProxy.newProxyInstance(Wiring.class,
                        configuration.getComponentUnit().getClassLoader(),
                        new AnnotationValueResolver() {
                    public Object getValue(Method method, ClassLoader loader) 
                            throws Throwable {
                        if (method.getName().equals("value")) {
                            return new WiringMode[] {BY_NAME, BY_TYPE};
                        } else {
                            return method.getDefaultValue();
                        }
                    }
                });
            }
            supported = isWiringModeSupported(ea, defaultWiring);
        } else {
            supported = false;
        }
        return supported;
    }
}
