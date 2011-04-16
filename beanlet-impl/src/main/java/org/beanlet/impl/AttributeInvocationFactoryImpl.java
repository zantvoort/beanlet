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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.beanlet.impl.Attributes.Getter;
import org.beanlet.impl.Attributes.Setter;
import org.beanlet.event.AttributeReadEvent;
import org.beanlet.event.AttributeWriteEvent;
import org.beanlet.common.event.AttributeReadEventImpl;
import org.beanlet.common.event.AttributeWriteEventImpl;
import org.beanlet.common.InvocationImpl;
import org.jargo.ComponentConfiguration;
import org.jargo.Event;
import org.jargo.InvocationFactory;
import org.jargo.Invocation;

/**
 * @author Leon van Zantvoort
 */
public final class AttributeInvocationFactoryImpl implements 
        InvocationFactory {
    
    private final Attributes attributes;
    private final Set<Method> methods;
    
    public AttributeInvocationFactoryImpl(ComponentConfiguration configuration) {
        attributes = Attributes.getInstance(configuration);
        Set<Method> tmp = new HashSet<Method>();
        for (Getter getter : attributes.getGetters()) {
            if (getter.getMember() instanceof Method) {
                tmp.add((Method) getter.getMember());
            }
        }
        for (Setter setter : attributes.getSetters()) {
            if (setter.getMember() instanceof Method) {
                tmp.add((Method) setter.getMember());
            }
        }
        methods = Collections.unmodifiableSet(tmp);
    }
    
    public Set<Method> getMethods() {
        return methods;
    }

    public Invocation getInvocation(Event event) {
        final Invocation invocation;
        if (event instanceof AttributeReadEvent) {
            AttributeReadEvent attributeEvent = (AttributeReadEvent) event;  
            
            final Getter getter = attributes.getGetter(attributeEvent.getAttributeName());
            if (getter != null) {
                Method method = null;
                if (getter.getMember() instanceof Method) {
                    method = (Method) getter.getMember();
                }
                invocation = new InvocationImpl(method) {
                    public Object invoke(Object instance) throws Exception {
                        return getter.get(instance);
                    }
                };
            } else {
                invocation = null;
            }
        } else if (event instanceof AttributeWriteEvent) {
            AttributeWriteEvent attributeEvent = (AttributeWriteEvent) event;
            
            final Setter setter = attributes.getSetter(attributeEvent.getAttributeName());
            final Object value = attributeEvent.getAttributeValue();
            if (setter != null) {
                Method method = null;
                if (setter.getMember() instanceof Method) {
                    method = (Method) setter.getMember();
                }
                invocation = new InvocationImpl(method) {
                    public Object invoke(Object instance) throws Exception {
                        setter.set(instance, value);
                        return null;
                    }
                };
            } else {
                invocation = null;
            }
        } else {
            assert false;
            invocation = null;
        }
        return invocation;
    }

    public Set<Class<? extends Event>> getEventTypes() {
        Set<Class<? extends Event>> c = 
                new LinkedHashSet<Class<? extends Event>>();
        c.add(AttributeReadEventImpl.class);
        c.add(AttributeWriteEventImpl.class);
        return Collections.unmodifiableSet(c);
    }
}
