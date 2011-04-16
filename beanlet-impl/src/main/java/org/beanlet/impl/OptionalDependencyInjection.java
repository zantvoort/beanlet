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

import java.util.Collections;
import java.util.Set;
import org.beanlet.BeanletWiringException;
import org.beanlet.annotation.Element;
import org.beanlet.plugin.DependencyInjection;
import org.beanlet.plugin.Injectant;
import org.jargo.ComponentContext;


/**
 * This class makes life of the {@code InjectionFactoryImpl} a little easier. 
 * This {@code DependencyInjection} handles 
 * {@code BeanletWiringException}s at {@code getInjectant}, by replacing these
 * exceptions for nill injectants if {@code isOptional} returns {@code true}.
 *
 * @author Leon van Zantvoort
 */
public class OptionalDependencyInjection implements DependencyInjection {
    
    private final DependencyInjection injection;
    
    public OptionalDependencyInjection(DependencyInjection injection) {
        this.injection = injection;
    }
    
    public boolean isOptional() {
        return injection.isOptional();
    }

    public Element getTarget() {
        return injection.getTarget();
    }

    public Set<String> getDependencies() throws BeanletWiringException {
        try {
            return injection.getDependencies();
        } catch (BeanletWiringException e) {
            if (isOptional()) {
                return Collections.emptySet();
            } else {
                throw e;
            }
        }
    }
    
    public Injectant<?> getInjectant(ComponentContext<?> ctx) throws BeanletWiringException {
        try {
            return injection.getInjectant(ctx);
        } catch (BeanletWiringException e) {
            if (isOptional()) {
                return null;
            } else {
                throw e;
            }
        }
    }
}