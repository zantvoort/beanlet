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
package org.beanlet.impl.metadata;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import org.beanlet.common.AbstractMetaData;
import org.jargo.MetaData;
import org.beanlet.metadata.OperationMetaData;

/**
 *
 * @author Leon van Zantvoort
 */
public final class OperationMetaDataImpl extends AbstractMetaData implements
        OperationMetaData, MetaData {
    
    private final String operationName;
    private final Class<?> returnType;
    private final Class<?>[] parameterTypes;
    private final String attributeName;
    private final boolean getter;
    private final boolean setter;
    private final boolean is;
    
    public OperationMetaDataImpl(String operationName, AnnotatedElement element, 
            Class<?> returnType, Class<?>[] parameterTypes, 
            String attributeName, boolean getter, boolean setter, boolean is,
            String description) {
        super(element, description);
        this.operationName = operationName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes.clone();
        this.attributeName = attributeName;
        this.getter = getter;
        this.setter = setter;
        this.is = is;
    }
    
    public String getOperationName() {
        return operationName;
    }
    
    public Class<?> getReturnType() {
        return returnType;
    }
    
    public Class<?>[] getParameterTypes() {
        return parameterTypes.clone();
    }
    
    public String toString() {
        return "OperationMetaData{operationName=" + getOperationName() + ", returnType=" +
                getReturnType() + ", parameterTypes=" + Arrays.asList(getParameterTypes()) +
                ", description=" + getDescription() + "}";
    }

    public String getAttributeName() {
        return attributeName;
    }
    
    public boolean isGetter() {
        return getter;
    }

    public boolean isSetter() {
        return setter;
    }

    public boolean isIs() {
        return is;
    }
}