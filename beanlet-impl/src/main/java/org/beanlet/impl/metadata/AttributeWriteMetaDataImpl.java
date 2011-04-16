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
import org.beanlet.common.AbstractMetaData;
import org.jargo.MetaData;
import org.beanlet.metadata.AttributeWriteMetaData;

/**
 *
 * @author Leon van Zantvoort
 */
public final class AttributeWriteMetaDataImpl extends AbstractMetaData implements 
        AttributeWriteMetaData, MetaData {
    
    private final String attributeName;
    private final Class<?> type;
    private final String operationName;
    
    public AttributeWriteMetaDataImpl(String attributeName, AnnotatedElement element, 
            Class<?> type, String operationName, String description) {
        super(element, description);
        this.attributeName = attributeName;
        this.type = type;
        this.operationName = operationName;
    }
    
    public String getAttributeName() {
        return attributeName;
    }
    
    public Class<?> getType() {
        return type;
    }

    public String getOperationName() {
        return operationName;
    }
    
    public String toString() {
        return "AttributeWriteMetaData{attributeName=" + getAttributeName() + 
                ", type=" + getType() + 
                ", description=" + getDescription() + "}";
    }
}