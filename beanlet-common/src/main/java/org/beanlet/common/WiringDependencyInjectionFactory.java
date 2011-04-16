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
import java.util.Arrays;
import org.beanlet.WiringMode;
import org.beanlet.Inject;
import org.beanlet.Wiring;
import org.beanlet.annotation.AnnotationDeclaration;
import org.beanlet.annotation.AnnotationProxy;
import org.beanlet.annotation.ConstructorElement;
import org.beanlet.annotation.ConstructorParameterElement;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.annotation.MethodElement;
import org.beanlet.annotation.MethodParameterElement;
import org.beanlet.annotation.PackageElement;
import org.beanlet.annotation.TypeElement;

/**
 *
 * @author Leon van Zantvoort
 */
public abstract class WiringDependencyInjectionFactory extends
        ParameterizedTypeAwareDependencyInjectionFactory {
    
    private final BeanletConfiguration<?> configuration;
    
    public WiringDependencyInjectionFactory(
            BeanletConfiguration<?> configuration) {
        super(configuration);
        this.configuration = configuration;
    }
    
    /**
     * Returns the wiring mode supported by this factory.
     */
    public abstract WiringMode getWiringMode();
    
    /**
     * Returns {@code true} if specified element supports the wiring method
     * expressed by this factory.
     */
    public boolean isWiringModeSupported(
            ElementAnnotation<? extends Element, Inject> ea) {
        return isWiringModeSupported(ea, 
                AnnotationProxy.newProxyInstance(Wiring.class));
    }
    
    /**
     * Returns {@code true} if specified element supports the wiring method
     * expressed by this factory.
     */
    protected boolean isWiringModeSupported(
            ElementAnnotation<? extends Element, Inject> ea, 
            Wiring defaultWiring) {
        final Wiring wiring;
        AnnotationDeclaration<Wiring> declaration = configuration.
                getAnnotationDomain().getDeclaration(Wiring.class);
        Wiring tmp = declaration.getDeclaredAnnotation(ea.getElement());
        if (tmp != null) {
            wiring = tmp;
        } else {
            Element e = ea.getElement();
            if (e instanceof MethodParameterElement) {
                tmp = configuration.getAnnotationDomain().
                        getDeclaration(Wiring.class).getAnnotation(
                        MethodElement.instance(
                        ((MethodParameterElement) e).getMethod()));
            } else if (e instanceof ConstructorParameterElement) {
                tmp = configuration.getAnnotationDomain().
                        getDeclaration(Wiring.class).getAnnotation(
                        ConstructorElement.instance(
                        ((ConstructorParameterElement) e).getConstructor()));
            }
            if (tmp != null) {
                wiring = tmp;
            } else {
                Class<?> type = ea.getElement().getMember().getDeclaringClass();
                tmp = declaration.getDeclaredAnnotation(
                        TypeElement.instance(type));
                if (tmp != null) {
                    wiring = tmp;
                } else {
                    tmp = declaration.getDeclaredAnnotation(PackageElement.
                            instance(type.getPackage()));
                    if (tmp != null) {
                        wiring = tmp;
                    } else {
                        wiring = defaultWiring;
                    }
                }
            }
        }
        return Arrays.asList(wiring.value()).contains(getWiringMode());
    }
}
