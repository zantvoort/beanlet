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
package org.beanlet.management.impl;

import static org.beanlet.management.ManageableElementType.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import org.beanlet.metadata.AttributeReadMetaData;
import org.beanlet.metadata.AttributeWriteMetaData;
import org.beanlet.management.ManageableElementType;
import org.jargo.ComponentMetaData;
import org.jargo.ComponentReference;
import org.jargo.MetaData;
import org.beanlet.metadata.OperationMetaData;
import org.beanlet.management.ExposeElement;
import org.beanlet.management.HideElement;
import org.beanlet.management.Manageable;

/**
 *
 * @author Leon van Zantvoort
 */
public class BeanletMBeanInfoFactory {
    
    public static MBeanInfo getMBeanInfo(ComponentReference reference, 
            Manageable manageable) {
        ComponentMetaData metaData = reference.getComponentMetaData();
        Set<ManageableElementType> types = new HashSet<ManageableElementType>(
                Arrays.asList(manageable.exposed()));
        return new MBeanInfo(metaData.getType().getName(),
                reference.getComponentMetaData().getDescription(),
                getAttributeInfo(reference, types.contains(ATTRIBUTE)),
                getConstructorInfo(reference),
                getOperationInfo(reference, types.contains(OPERATION)),
                getNotificationInfo(reference));
    }
    
    private static MBeanAttributeInfo[] getAttributeInfo(ComponentReference<?> 
            reference, boolean expose) {        
        List<MBeanAttributeInfo> mbeanInfos = new ArrayList<MBeanAttributeInfo>();
        
        Map<String, AttributeReadMetaData> readable = 
                new HashMap<String, AttributeReadMetaData>();
        Map<String, AttributeWriteMetaData> writeable = 
                new HashMap<String, AttributeWriteMetaData>();
        Set<String> all = new HashSet<String>();
        for (MetaData m : reference.getComponentMetaData().getMetaData()) {
            if (m instanceof AttributeReadMetaData) {
                AttributeReadMetaData r = (AttributeReadMetaData) m;
                boolean add = expose;
                if (!add && r.isAnnotationPresent(ExposeElement.class)) {
                    add = true;
                }
                if (add && r.isAnnotationPresent(HideElement.class)) {
                    add = false;
                }
                if (add) {
                    readable.put(r.getAttributeName(), r);
                    all.add(r.getAttributeName());
                }
            }
            if (m instanceof AttributeWriteMetaData) {
                AttributeWriteMetaData w = (AttributeWriteMetaData) m;
                boolean add = expose;
                if (!add && w.isAnnotationPresent(ExposeElement.class)) {
                    add = true;
                }
                if (add && w.isAnnotationPresent(HideElement.class)) {
                    add = false;
                }
                if (add) {
                    writeable.put(w.getAttributeName(), w);
                    all.add(w.getAttributeName());
                }
            }
        }
        for (String name : all) {
            AttributeReadMetaData r = readable.get(name);
            AttributeWriteMetaData w = writeable.get(name);
            
            assert r != null || w != null;
            
            String type = r != null ? r.getType().getName() : w.getType().getName();
            String description = r != null ? r.getDescription() : w.getDescription();
            mbeanInfos.add(new MBeanAttributeInfo(name, type, description,
                    r != null, w != null, r != null && r.isIs()));
        }
        return mbeanInfos.toArray(new MBeanAttributeInfo[0]);
    }
    
    private static MBeanConstructorInfo[] getConstructorInfo(
            ComponentReference<?> reference) {
        return new MBeanConstructorInfo[0];
    }
    
    private static MBeanOperationInfo[] getOperationInfo(
            ComponentReference<?> reference, boolean expose) {
        List<MBeanOperationInfo> mbeanInfos = 
                new ArrayList<MBeanOperationInfo>();
        
        for (MetaData metaData : reference.getComponentMetaData().getMetaData()) {
            if (metaData instanceof OperationMetaData) {
                OperationMetaData m = (OperationMetaData) metaData;
                if (!m.isGetter() && !m.isSetter()) { 
                    // This feature prevents that getter and setter operations 
                    // are exposed.
                    boolean add = expose;
                    if (!add && m.isAnnotationPresent(ExposeElement.class)) {
                        add = true;
                    }
                    if (add && m.isAnnotationPresent(HideElement.class)) {
                        add = false;
                    }
                    if (add) {
                        MBeanParameterInfo[] signature = 
                                new MBeanParameterInfo[m.getParameterTypes().length];
                        for (int i = 0; i < signature.length; i++) {
                            signature[i] = new MBeanParameterInfo("p" + (i + 1), 
                                    m.getParameterTypes()[i].getName(), "");
                        }
                        mbeanInfos.add(new MBeanOperationInfo(m.getOperationName(), 
                                m.getDescription(), signature, 
                                m.getReturnType().getName(),
                                MBeanOperationInfo.UNKNOWN));
                    }
                }
            }
            // Clients can expose proxy method by exposing it as an operation as well.
//            if (metaData instanceof ProxyMethodMetaData) {
//                boolean add = expose;
//                if (!add && ((ProxyMethodMetaData) metaData).isAnnotationPresent(ExposeElement.class)) {
//                    add = true;
//                }
//                if (add && ((ProxyMethodMetaData) metaData).isAnnotationPresent(HideElement.class)) {
//                    add = false;
//                }
//                if (add) {
//                    MBeanParameterInfo[] signature =
//                            new MBeanParameterInfo[3];
//                    signature[0] = new MBeanParameterInfo("name", String.class.getName(), "");
//                    signature[1] = new MBeanParameterInfo("parameterTypes", String[].class.getName(), "");
//                    signature[2] = new MBeanParameterInfo("parameters", Object[].class.getName(), "");
//                    mbeanInfos.add(new MBeanOperationInfo("invoke",
//                            ((ProxyMethodMetaData) metaData).getDescription(),
//                            signature, Object.class.getName(),
//                            MBeanOperationInfo.UNKNOWN));
//                }
//            }
        }
        return mbeanInfos.toArray(new MBeanOperationInfo[mbeanInfos.size()]);
    }
    
    private static MBeanNotificationInfo[] getNotificationInfo(ComponentReference 
            reference) {
        return new MBeanNotificationInfo[0];
    }
}
