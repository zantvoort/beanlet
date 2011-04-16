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
package org.beanlet.common.event;

import java.util.Arrays;
import org.beanlet.event.OperationEvent;
import org.jargo.Event;

/**
 * @author Leon van Zantvoort
 */
public final class OperationEventImpl implements OperationEvent, Event {
    
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
    
    private final String name;
    private final Class<?>[] parameterTypes;
    private final Object[] parameters;

    public OperationEventImpl() {
        this(null, null);
    }
    
    public OperationEventImpl(String name, Class<?>[] parameterTypes, 
            Object... args) {
        this.name = name;
        this.parameterTypes = (parameterTypes == null ? EMPTY_CLASS_ARRAY : 
                parameterTypes.clone());
        this.parameters = (args.length == 0 ? EMPTY_OBJECT_ARRAY : args.clone());
    }
    
    public String getOperationName() {
        return name;
    }
    
    public OperationEvent setOperationName(String name) {
        return new OperationEventImpl(name, parameterTypes, parameters);
    }
    
    public Class<?>[] getParameterTypes() {
        return parameterTypes.clone();
    }
    
    public OperationEvent setParameterTypes(Class<?>[] parameterTypes) {
        return new OperationEventImpl(name, parameterTypes);
    }

    public Object[] getParameters() {
        return parameters.clone();
    }
    
    public OperationEvent setParameters(Object... args) {
        return new OperationEventImpl(name, parameterTypes, args);
    }

    public String toString() {
        return "OperationEvent{name=" + getOperationName() + ", " + 
                Arrays.asList(getParameterTypes()) + "}@" + 
                Integer.toHexString(System.identityHashCode(this));
    }
}
