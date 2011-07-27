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

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.beanlet.BeanletWiringException;
import org.beanlet.annotation.Element;
import org.beanlet.plugin.DependencyInjection;
import org.beanlet.plugin.Injectant;
import org.jargo.ComponentContext;

/**
 * @author Leon van Zantvoort
 */
public class CachingDependencyInjection implements DependencyInjection {

    private final DependencyInjection injection;
    private final Element target;
    private final boolean optional;
    private final Set<String> dependencies;
    
    private final AtomicReference<Injectant> injectant;
    private final AtomicReference<BeanletWiringException> exception;
    
    public CachingDependencyInjection(DependencyInjection injection) {
        this.injection = injection;
        this.target = injection.getTarget();
        this.optional = injection.isOptional();
        this.dependencies = injection.getDependencies();
        this.injectant = new AtomicReference<Injectant>();
        this.exception = new AtomicReference<BeanletWiringException>();
    }
    
    public boolean isOptional() {
        return optional;
    }

    public Element getTarget() {
        return target;
    }

    public Set<String> getDependencies() throws BeanletWiringException {
        return dependencies;
    }
    
    public Injectant<?> getInjectant(ComponentContext<?> ctx) throws 
            BeanletWiringException {
        Injectant i = injectant.get();
        if (i != null) {
            return i;
        }
        BeanletWiringException e = exception.get();
        if (e != null) {
            throw e;
        }
        try {
            i = injection.getInjectant(ctx);
            if (i != null && i.isCacheable()) {
                // Don't store negatives.
                injectant.compareAndSet(null, i);
                return injectant.get();
            }
            return i;
        } catch (BeanletWiringException ex) {
            exception.compareAndSet(null, ex);
            throw exception.get();
        }
    }
}