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
package org.beanlet.persistence.impl;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.PersistenceContext;
import org.jargo.ComponentUnit;
import org.jargo.InvocationContext;
import org.jargo.InvocationInterceptor;

/**
 * @author Leon van Zantvoort
 */
public final class EntityManagerInvocationInterceptor implements
        InvocationInterceptor {
    
    private final Set<BeanletPersistenceContext> contexts;
    
    public EntityManagerInvocationInterceptor(
            Set<PersistenceContext> pctxs, ComponentUnit componentUnit) {
        contexts = new HashSet<BeanletPersistenceContext>();
        for (PersistenceContext pctx : pctxs) {
            BeanletPersistenceContext context = 
                    BeanletPersistenceContext.getInstance(pctx, componentUnit);
            contexts.add(context);
        }
    }
    
    public Object intercept(InvocationContext ctx) throws Exception {
        boolean commit = true;
        try {
            for (BeanletPersistenceContext pctx : contexts) {
                pctx.preInvoke();
            }
            return ctx.proceed();
        } catch (RuntimeException e) {
            commit = false;
            throw e;
        } finally {
            for (BeanletPersistenceContext pctx : contexts) {
                pctx.postInvoke(commit);
            }
        }
    }
    
    public boolean isLifecycleInterceptor() {
        return false;
    }
}
