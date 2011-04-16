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

import java.util.Set;
import org.beanlet.BeanletWiringException;
import org.beanlet.annotation.Element;
import org.jargo.ComponentContext;

/**
 *
 * @author Leon van Zantvoort
 */
public interface DependencyInjection {

    /**
     * Returns the member element that is target of dependency injection.
     */
    Element getTarget();
    
    /**
     * Returns {@code true} if the dependency does not need to be enforced. In
     * this case, the dependecy is merely used for specifying startup order.
     */
    boolean isOptional();

    /**
     * Returns the beanlet names upon which this member has a dependency, 
     * an empty set if no dependency exist, or {@code null} if information is
     * not available.
     */
    Set<String> getDependencies() throws BeanletWiringException;
    
    /**
     * Returns the injectant, or {@code null} if no injectant was found for the
     * specified context. Let the injectant return {@code null} to inject a 
     * {@code null} value.
     */
    Injectant<?> getInjectant(ComponentContext<?> ctx) throws 
            BeanletWiringException;
}
