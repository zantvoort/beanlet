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

import org.beanlet.common.event.StartEventImpl;
import org.jargo.ComponentFactory;
import org.jargo.ComponentReferenceLifecycle;
import org.jargo.ComponentLifecycle;
import org.jargo.ComponentReference;
import org.jargo.Event;

public final class StartBeanletLifecycleImpl<T> implements 
        ComponentLifecycle<T>, ComponentReferenceLifecycle<T> {

    private static final Event event = new StartEventImpl();
    
    public void onCreate(ComponentFactory<T> factory) {
        if (factory.getComponentMetaData().isStatic()) {
            ComponentReference<T> reference = factory.create();
            assert reference.isExecutable(event);
            reference.execute(event);
        }
    }
    
    public void onDestroy(ComponentFactory<T> factory) {
    }

    public void onCreate(ComponentReference<T> reference) {
        if (!reference.getComponentMetaData().isStatic()) {
            assert reference.isExecutable(event);
            reference.execute(event);
        }
    }
    
    public void onDestroy(ComponentReference<T> reference) {
    }
}
