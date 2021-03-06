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

import static org.beanlet.common.Beanlets.*;
import org.beanlet.BeanletStateException;
import org.jargo.ComponentContext;
import org.beanlet.BeanletContext;
import org.beanlet.BeanletMetaData;
import org.beanlet.BeanletReference;
import org.jargo.ComponentStateException;

/**
 *
 * @author Leon van Zantvoort
 */
public final class BeanletContextImpl<T> implements BeanletContext<T> {
    
    private final ComponentContext<T> ctx;
    private final BeanletMetaData<T> metaData;
    
    public static BeanletContext<?> instance(ComponentContext<?> ctx) {
        @SuppressWarnings("unchecked")
        ComponentContext<Object> c = (ComponentContext<Object>) ctx;
        return new BeanletContextImpl<Object>(c);
    }
    
    public BeanletContextImpl(ComponentContext<T> ctx) {
        this.ctx = ctx;
        this.metaData = new BeanletMetaDataImpl<T>(ctx.getComponentMetaData());
    }
    
    public BeanletMetaData<T> getBeanletMetaData() {
        return metaData;
    }
    
    public BeanletReference<T> reference() throws BeanletStateException {
        try {
            return new BeanletReferenceImpl<T>(ctx.reference());
        } catch (ComponentStateException e) {
            throw new BeanletStateException(e.getComponentName(),
                    CHAIN_JARGO_EXCEPTIONS ? e : e.getCause());
        }
    }
}