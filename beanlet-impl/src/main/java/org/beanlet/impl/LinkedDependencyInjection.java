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
import java.util.List;
import java.util.Set;
import org.beanlet.BeanletWiringException;
import org.beanlet.annotation.Element;
import org.beanlet.plugin.DependencyInjection;
import org.beanlet.plugin.Injectant;
import org.jargo.ComponentContext;


/**
 * @author Leon van Zantvoort
 */
public class LinkedDependencyInjection implements DependencyInjection {

    private final List<DependencyInjection> injections;
    
    public LinkedDependencyInjection(List<DependencyInjection> injections) {
        assert !injections.isEmpty();
        assert assertTarget(injections);
        this.injections = injections;
    }
    
    public boolean isOptional() {
        boolean optional = true;
        for(DependencyInjection injection : injections) {
            if (optional) {
                optional = injection.isOptional();
            }
            if (!optional) {
                break;
            }
        }
        return optional;
    }

    public Element getTarget() {
        return injections.get(0).getTarget();
    }

    public Set<String> getDependencies() throws BeanletWiringException {
        Set<String> dependencies = null;
        for(DependencyInjection injection : injections) {
            dependencies = injection.getDependencies();
            if (dependencies != null) {
                break;
            }
        }
        return dependencies == null ?
                Collections.<String>emptySet() : 
                Collections.unmodifiableSet(dependencies);
    }
    
    public Injectant<?> getInjectant(ComponentContext<?> ctx) throws BeanletWiringException {
        Injectant<?> injectant = null;
        for(DependencyInjection injection : injections) {
            injectant = injection.getInjectant(ctx);
            if (injectant != null) {
                break;
            }
        }
        return injectant;
    }

    private boolean assertTarget(List<DependencyInjection> injections) {
        Element target = null;
        for (DependencyInjection injection : injections) {
            Element tmp = injection.getTarget();
            if (target == null) {
                target = tmp;
            } else {
                if (!target.equals(tmp)) {
                    return false;
                }
            }
        }
        return true;
    }
}