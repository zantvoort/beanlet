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

import org.beanlet.plugin.Injectant;
import org.beanlet.plugin.BeanletConfiguration;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;
import org.beanlet.BeanletWiringException;
import org.jargo.ComponentContext;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.annotation.ParameterElement;

/**
 *
 * @author Leon van Zantvoort
 */
public class DependencyInjectionFactoryTerminator<T extends Annotation>
        extends AbstractDependencyInjectionFactory<T> {
    
    private final BeanletConfiguration<?> configuration;
    private final Class<T> annotationType;
    
    public DependencyInjectionFactoryTerminator(
            BeanletConfiguration<?> configuration, Class<T> annotationType) {
        super(configuration);
        this.configuration = configuration;
        this.annotationType = annotationType;
    }
    
    public Class<T> annotationType() {
        return annotationType;
    }
    
    public boolean isSupported(ElementAnnotation<? extends Element, T> ea) {
        return true;
    }
    
    public Set<String> getDependencies(ElementAnnotation<? extends Element, T> ea) {
        return Collections.emptySet();
    }
    
    public boolean isOptional(ElementAnnotation<? extends Element, T> ea) {
        return false;
    }
    
    public Injectant<?> getInjectant(
            final ElementAnnotation<? extends Element, T> ea,
            final ComponentContext<?> ctx) {
        if (isOptional(ea)) {
            return null;
        } else {
            final String message;
            if (ea.getElement() instanceof ParameterElement) {
                message = "No injectant found for argument " + 
                        ((ParameterElement) ea.getElement()).getParameter() + 
                        " of member.";
            } else {
                message = "No injectant found for member.";
            }
            throw new BeanletWiringException(
                    configuration.getComponentName(),
                    ea.getElement().getMember(), message);
        }
    }
}
