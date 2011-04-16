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
package org.beanlet.plugin;

import org.beanlet.annotation.AnnotationDomain;
import org.jargo.ComponentConfiguration;

/**
 * Exposes the beanlet configuration variables. 
 *
 * @author Leon van Zantvoort
 */
public interface BeanletConfiguration<T> extends ComponentConfiguration<T> {

    /**
     * Returns the name of the beanlet that is responsible for creating beanlet
     * instances for this beanlet. Unless a factory method is defined, objects 
     * returned by {@BeanletApplicationContext.getBeanlet} become beanlet 
     * instances for this beanlet.
     * 
     * @return name of the beanlet that is responsible for creating beanlet
     * instances, or {@code null} if no factory is configured.
     */
    String getFactory();
    
    /**
     * Returns the method name that of the beanlet specified by 
     * {@code getFactory} that is responsible for creating beanlet instances for
     * this beanlet.
     * 
     * @return method name that is to be invoked on the beanlet specified
     * by {@code getFactory}, or {@code null} if no factory method is 
     * configured.
     */
    String getFactoryMethod();
    
    /**
     * Provides access to the annotations associated with this beanlet.
     */
    AnnotationDomain getAnnotationDomain();
}
