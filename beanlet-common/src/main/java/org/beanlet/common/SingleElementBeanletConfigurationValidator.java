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
import java.util.ArrayList;
import java.util.List;
import org.beanlet.BeanletValidationException;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;

/**
 *
 * @author Leon van Zantvoort
 */
public abstract class 
        SingleElementBeanletConfigurationValidator<T extends Annotation> extends
        AbstractBeanletConfigurationValidator<T> {

    @Override
    public void validate(BeanletConfiguration configuration) throws
            BeanletValidationException {
        List<ElementAnnotation<Element, T>> list = configuration.
                getAnnotationDomain().getDeclaration(annotationType()).
                getElements(configuration.getType());
        if (list.size() > 1) {
            List<Element> elements = new ArrayList<Element>();
            for (ElementAnnotation<Element, T> e : list) {
                elements.add(e.getElement());
            }
            throw new BeanletValidationException(configuration.getComponentName(),
                    "Beanlet type MAY mark only one element with the " + 
                    annotationType().getSimpleName() + " annotation: " + 
                    elements + ".");
        }
        super.validate(configuration);
    }
}
