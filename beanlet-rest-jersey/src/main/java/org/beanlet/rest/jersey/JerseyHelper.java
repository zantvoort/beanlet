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
package org.beanlet.rest.jersey;

import org.beanlet.BeanletReference;
import org.beanlet.common.BeanletStack;

public final class JerseyHelper {

    private static final InheritableThreadLocal<Object> jerseyObjectLocal = new InheritableThreadLocal<Object>();
    private static final InheritableThreadLocal<String> beanletNameLocal = new InheritableThreadLocal<String>();
    private static final InheritableThreadLocal<BeanletStack<BeanletReference<?>>> beanletReferenceLocal = new InheritableThreadLocal<BeanletStack<BeanletReference<?>>>() {
        @Override
        protected BeanletStack<BeanletReference<?>> initialValue() {
            return new BeanletStack<BeanletReference<?>>();
        }
    };

    private JerseyHelper() {
    }

    public static void pushBeanletReference(BeanletReference<?> reference) {
        beanletReferenceLocal.get().push(reference);
    }
    
    public static void setJerseyObject(String beanletName, Object o) {
        beanletNameLocal.set(beanletName);
        jerseyObjectLocal.set(o);
    }
    
    public static Object getJerseyObject() {
        return jerseyObjectLocal.get();
    }
    
    public static BeanletReference<?> popBeanletReference() {
        return beanletReferenceLocal.get().pop();
    }

    public static int getBeanletReferenceCount() {
        return beanletReferenceLocal.get().size();
    }

    /**
     * Returns true if component is a jersey restlet.
     *
     * @param beanletName
     * @return
     */
    public static boolean isJerseyObject(String beanletName) {
        return beanletName.equals(beanletNameLocal.get());
    }
}
