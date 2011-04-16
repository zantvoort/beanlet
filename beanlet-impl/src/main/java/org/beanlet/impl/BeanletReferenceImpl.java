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
import org.beanlet.BeanletEventException;
import org.beanlet.BeanletEventNotExecutableException;
import org.beanlet.BeanletExecutionException;
import org.beanlet.BeanletMetaData;
import org.beanlet.BeanletNotOfRequiredTypeException;
import org.jargo.ComponentReference;
import org.beanlet.BeanletReference;
import org.beanlet.Event;
import org.jargo.ComponentEventException;
import org.jargo.ComponentEventNotExecutableException;
import org.jargo.ComponentExecutionException;

/**
 *
 * @author Leon van Zantvoort
 */
public final class BeanletReferenceImpl<T> implements BeanletReference<T> {

    private final ComponentReference<T> reference;
    private final BeanletMetaData<T> metaData;
    
    public static BeanletReference<?> instance(ComponentReference<?> reference) {
        @SuppressWarnings("unchecked")
        ComponentReference<Object> r = (ComponentReference<Object>) reference;
        return new BeanletReferenceImpl<Object>(r);
    }    
    
    public BeanletReferenceImpl(ComponentReference<T> reference) {
        this.reference = reference;
        this.metaData = new BeanletMetaDataImpl<T>(reference.getComponentMetaData());
    }

    public BeanletMetaData<T> getBeanletMetaData() {
        return metaData;
    }

    public void invalidate() {
        reference.invalidate();
    }

    public boolean isValid() {
        return reference.isValid();
    }
    
    public void remove() {
        reference.remove();
    }
    
    public boolean isRemoved() {
        return reference.isRemoved();
    }

    public Object getBeanlet() {
        return reference.getComponent();
    }
    
    public T getTypedBeanlet() throws BeanletNotOfRequiredTypeException {
        Object tmp = getBeanlet();
        if (tmp == null) {
            return null;
        } else {
            if (metaData.getType().isAssignableFrom(tmp.getClass())) {
                @SuppressWarnings("unchecked")
                T t = (T) tmp;
                return t;
            } else {
                throw new BeanletNotOfRequiredTypeException(
                        metaData.getBeanletName(), metaData.getType(), 
                        tmp.getClass());
            }
        }
    }

    public boolean isExecutable(Event event) {
        if (event instanceof org.jargo.Event) {
            return reference.isExecutable((org.jargo.Event) event);
        } else {
            return false;
        }
    }

    public Object execute(Event event) throws BeanletEventException {
        if (event instanceof org.jargo.Event) {
            try {
                return reference.execute((org.jargo.Event) event);
            } catch (ComponentExecutionException e2) {
                Throwable t = e2.getCause();
                assert t != null;
                throw new BeanletExecutionException(e2.getComponentName(), 
                        event, t);
            } catch (ComponentEventNotExecutableException e2) {
                throw new BeanletEventNotExecutableException(
                        
                        e2.getComponentName(), event, 
                        CHAIN_JARGO_EXCEPTIONS ? e2 : e2.getCause());
            } catch (ComponentEventException e2) {
                throw new BeanletEventException(e2.getComponentName(), event, 
                        CHAIN_JARGO_EXCEPTIONS ? e2 : e2.getCause());
            } catch (RuntimeException e2) {
                throw new BeanletEventException(
                        
                        getBeanletMetaData().getBeanletName(), event, 
                        CHAIN_JARGO_EXCEPTIONS ? e2 : e2.getCause());
            }
        } else {
            throw new BeanletEventNotExecutableException(getBeanletMetaData().
                    getBeanletName(), event);
        }
    }
    
    public boolean equals(Object obj) {
        final boolean value;
        if (obj instanceof BeanletReferenceImpl) {
            value = reference.equals(((BeanletReferenceImpl) obj).reference);
        } else {
            value = false;
        }
        return value;
    }

    public int hashCode() {
        return reference.hashCode();
    }
}
