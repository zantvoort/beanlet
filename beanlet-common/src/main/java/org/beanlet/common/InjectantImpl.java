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
import org.beanlet.BeanletWiringException;

/**
 *
 * @author Leon van Zantvoort
 */
public final class InjectantImpl<T> implements Injectant<T> {
    
    private final T injectant;
    private final boolean cacheable;
    private final boolean isStatic;

    public InjectantImpl() {
        this(null);
    }
    
    public InjectantImpl(T injectant) {
        this(injectant, false);
    }

    public InjectantImpl(T injectant, boolean cacheable) {
        this(injectant, cacheable, false);
    }

    public InjectantImpl(T injectant, boolean cacheable, boolean isStatic) {
        this.injectant = injectant;
        this.cacheable = cacheable;
        this.isStatic = isStatic;
    }

    public T getObject() {
        return injectant;
    }
    
    public boolean isCacheable() {
        return cacheable;
    }

    public boolean isStatic() {
        return isStatic;
    }
}
