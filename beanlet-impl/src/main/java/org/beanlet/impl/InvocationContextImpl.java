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
import java.util.HashMap;
import java.util.Map;
import org.beanlet.BeanletStateException;
import org.beanlet.InvocationContext;
import org.jargo.Invocation;

/**
 *
 * @author Leon van Zantvoort
 */
public abstract class InvocationContextImpl implements InvocationContext {
    
    protected abstract org.jargo.InvocationContext getInvocationContext();
    
    public Map<String, Object> getContextData() {
        org.jargo.InvocationContext ctx = getInvocationContext();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) ctx.get();
        if (map == null) {
            map = new HashMap<String, Object>();
            ctx.set(map);
        }
        return map;
    }
    
    public Method getMethod() {
        Invocation invocation = getInvocationContext().getInvocation();
        if (invocation == null) {
            throw new BeanletStateException(
                    getInvocationContext().getComponentContext().
                    getComponentMetaData().getComponentName(),
                    "Method not available for lifecycle interceptors.");
        }
        Method method = invocation.getMethod();
        assert method != null;
        return method;
    }

    public void setParameters(Object[] parameters) {
        Invocation invocation = getInvocationContext().getInvocation();
        if (invocation == null) {
            throw new BeanletStateException(
                    getInvocationContext().getComponentContext().
                    getComponentMetaData().getComponentName(),
                    "Parameters not available for lifecycle interceptors.");
        }
        invocation.setParameters(parameters.clone());
    }

    public Object[] getParameters() {
        Invocation invocation = getInvocationContext().getInvocation();
        if (invocation == null) {
            throw new BeanletStateException(
                    getInvocationContext().getComponentContext().
                    getComponentMetaData().getComponentName(),
                    "Parameters not available for lifecycle interceptors.");
        }
        return invocation.getParameters().clone();
    }
    
    public Object proceed() throws Exception {
        return getInvocationContext().proceed();
    }

    public Object getTarget() {
        return getInvocationContext().getTarget();
    }
}
