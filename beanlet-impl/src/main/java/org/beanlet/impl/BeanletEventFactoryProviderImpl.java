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

import java.util.Collections;
import java.util.List;
import org.beanlet.event.AttributeReadEvent;
import org.beanlet.event.AttributeWriteEvent;
import org.beanlet.Event;
import org.beanlet.event.FactoryEvent;
import org.beanlet.event.OperationEvent;
import org.beanlet.event.ExecuteEvent;
import org.beanlet.event.StartEvent;
import org.beanlet.event.StopEvent;
import org.beanlet.common.AbstractProvider;
import org.beanlet.common.event.AttributeReadEventImpl;
import org.beanlet.common.event.AttributeWriteEventImpl;
import org.beanlet.plugin.BeanletEventFactory;
import org.beanlet.common.event.FactoryEventImpl;
import org.beanlet.common.event.OperationEventImpl;
import org.beanlet.common.event.ExecuteEventImpl;
import org.beanlet.common.event.StartEventImpl;
import org.beanlet.common.event.StopEventImpl;
import org.beanlet.plugin.spi.BeanletEventFactoryProvider;

/**
 *
 * @author Leon van Zantvoort
 */
public final class BeanletEventFactoryProviderImpl 
        extends AbstractProvider implements BeanletEventFactoryProvider {
    
    private static final FactoryEvent FACTORY_EVENT = new FactoryEventImpl();
    private static final ExecuteEvent RUN_EVENT = new ExecuteEventImpl();
    private static final StartEvent START_EVENT = new StartEventImpl();
    private static final StopEvent STOP_EVENT = new StopEventImpl();
    
    public List<BeanletEventFactory> getBeanletEventFactories() {
        BeanletEventFactory factory = new BeanletEventFactory() {
            public <T extends Event> T getEvent(Class<T> eventClass) {
                final Event event;
                if (AttributeReadEvent.class.equals(eventClass)) {
                    event = new AttributeReadEventImpl();
                } else if (AttributeWriteEvent.class.equals(eventClass)) {
                    event = new AttributeWriteEventImpl();
                } else if (FactoryEvent.class.equals(eventClass)) {
                    event = FACTORY_EVENT;
                } else if (OperationEvent.class.equals(eventClass)) {
                    event = new OperationEventImpl();
                } else if (ExecuteEvent.class.equals(eventClass)) {
                    event = RUN_EVENT;
                } else if (StartEvent.class.equals(eventClass)) {
                    event = START_EVENT;
                } else if (StopEvent.class.equals(eventClass)) {
                    event = STOP_EVENT;
                } else {
                    event = null;
                }
                
                @SuppressWarnings("unchecked")
                T t = (T) event;
                return t;
            }
        };
        return Collections.singletonList(factory);
    }
}
