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

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;
import org.beanlet.management.Manageable;
import org.jargo.ComponentReference;
import org.beanlet.common.event.AttributeReadEventImpl;
import org.beanlet.common.event.AttributeWriteEventImpl;
import org.beanlet.common.event.OperationEventImpl;
import org.jargo.ComponentExecutionException;
import org.jargo.Event;

/**
 *
 * @author Leon van Zantvoort
 */
public final class BeanletDynamicMBean implements DynamicMBean {
  
    private final ClassLoader loader;
    private final Manageable manageable;
    private final ComponentReference reference;
    
    public BeanletDynamicMBean(ComponentReference reference,
            Class<?> type, ClassLoader loader, Manageable manageable) {
        this.loader = loader;
        this.manageable = manageable;
        this.reference = reference;
        
        // Failfast check of beanlet.
        getMBeanInfo();
    }

    public Object getAttribute(String attribute) throws 
            AttributeNotFoundException, MBeanException, ReflectionException {
        Event event = new AttributeReadEventImpl(attribute);
        if (!reference.isExecutable(event)) {
            throw new AttributeNotFoundException(attribute);
        }
        return reference.execute(event);
    }

    public void setAttribute(Attribute attribute) throws 
            AttributeNotFoundException, InvalidAttributeValueException, 
            MBeanException, ReflectionException {
        Event event = new AttributeWriteEventImpl(attribute.getName(), 
                attribute.getValue());
        if (!reference.isExecutable(event)) {
            throw new AttributeNotFoundException(attribute.getName());
        }
        reference.execute(event);
    }

    public AttributeList getAttributes(String[] attributes) {
        AttributeList list = new AttributeList();
        for (String attribute : attributes) {
            Event event = new AttributeReadEventImpl(attribute);
            if (reference.isExecutable(event)) {
                list.add(new Attribute(attribute, reference.execute(event)));
            }
        }
        return list;
    }
    
    public AttributeList setAttributes(AttributeList attributes) {
        for (Object attribute : attributes) {
            Event event = new AttributeWriteEventImpl(
                    ((Attribute) attribute).getName(), 
                    ((Attribute) attribute).getValue());
            if (reference.isExecutable(event)) {
                reference.execute(event);
            }
        }
        AttributeList list = new AttributeList();
        for (Object attribute : attributes) {
            Event event = new AttributeReadEventImpl(
                    ((Attribute) attribute).getName());;
            if (reference.isExecutable(event)) {
                list.add(new Attribute(((Attribute) attribute).getName(), 
                        reference.execute(event)));
            }
        }
        return list;
    }

    public Object invoke(String actionName, Object[] params, String[] signature) 
            throws MBeanException, ReflectionException {
        Class[] paramClasses = new Class[signature.length];
        try {
            for (int i = 0; i < signature.length; i++) {
                if (Boolean.TYPE.getName().equals(signature[i])) {
                    paramClasses[i] = Boolean.TYPE;
                } else if (Byte.TYPE.getName().equals(signature[i])) {
                    paramClasses[i] = Byte.TYPE;
                } else if (Short.TYPE.getName().equals(signature[i])) {
                    paramClasses[i] = Short.TYPE;
                } else if (Integer.TYPE.getName().equals(signature[i])) {
                    paramClasses[i] = Integer.TYPE;
                } else if (Long.TYPE.getName().equals(signature[i])) {
                    paramClasses[i] = Long.TYPE;
                } else if (Float.TYPE.getName().equals(signature[i])) {
                    paramClasses[i] = Float.TYPE;
                } else if (Double.TYPE.getName().equals(signature[i])) {
                    paramClasses[i] = Double.TYPE;
                } else {
                    paramClasses[i] = Class.forName(signature[i], true, loader);
                }
            }
            Event event = new OperationEventImpl(actionName, 
                    paramClasses, params);
            try {
                assert reference.isExecutable(event);
                return reference.execute(event);
            } catch (ComponentExecutionException e) {
                try {
                    throw e.getCause();
                } catch (Exception e2) {
                    throw new ReflectionException(e2);
                } catch (Error e2) {
                    throw e2;
                } catch (Throwable t) {
                    throw new AssertionError();
                }
            }
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    public MBeanInfo getMBeanInfo() {
        return BeanletMBeanInfoFactory.getMBeanInfo(reference, manageable);
    }
}
