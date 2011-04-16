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
import java.util.Map;
import org.beanlet.BeanletCreationException;
import org.jargo.ComponentFactory;
import org.beanlet.BeanletFactory;
import org.beanlet.BeanletMetaData;
import org.beanlet.BeanletNotActiveException;
import org.beanlet.BeanletReference;
import org.jargo.ComponentCreationException;
import org.jargo.ComponentNotActiveException;

/**
 *
 * @author Leon van Zantvoort
 */
public final class BeanletFactoryImpl<T> implements BeanletFactory<T> {

    private final ComponentFactory<T> factory;
    private final BeanletMetaData<T> metaData;
    
    public static BeanletFactory<?> instance(ComponentFactory<?> factory) {
        @SuppressWarnings("unchecked")
        ComponentFactory<Object> f = (ComponentFactory<Object>) factory;
        return new BeanletFactoryImpl<Object>(f);
    }
    
    public BeanletFactoryImpl(ComponentFactory<T> factory) {
        this.factory = factory;
        this.metaData = new BeanletMetaDataImpl<T>(factory.getComponentMetaData());
    }

    public BeanletMetaData<T> getBeanletMetaData() {
        return metaData;
    }
    
    public BeanletReference<T> create() throws BeanletCreationException {
        return create(null);
    }

    public BeanletReference<T> create(Map<String, ?> info) throws 
            BeanletCreationException {
        try {
            return new BeanletReferenceImpl<T>(factory.create(info));
        } catch (ComponentNotActiveException e) {
            throw new BeanletNotActiveException(e.getComponentName(), 
                    CHAIN_JARGO_EXCEPTIONS ? e : e.getCause());
        } catch (ComponentCreationException e) {
            throw new BeanletCreationException(e.getComponentName(), 
                    CHAIN_JARGO_EXCEPTIONS ? e : e.getCause());
        }
    }
}
