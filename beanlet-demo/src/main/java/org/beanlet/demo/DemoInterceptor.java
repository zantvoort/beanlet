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
package org.beanlet.demo;

import java.util.logging.Logger;
import org.beanlet.AroundInvoke;
import org.beanlet.Inject;
import org.beanlet.InvocationContext;
import org.beanlet.PostConstruct;
import org.beanlet.PreDestroy;

/**
 * <p>This interceptor demonstrates basic interceptor functionality. Interceptor 
 * instances are always bound to a single beanlet instance. They can wrap 
 * lifecycle- and/or business methods.</p>
 * 
 * <p>Similar to beanlets, interceptors support dependency injection.</p>
 * 
 * @author Leon van Zantvoort
 */
public final class DemoInterceptor {

    /**
     * This logger instance is automatically injected by the container.
     */
    @Inject
    private Logger logger;

    /**
     * This method interceptors the beanlet's post-construct method. This method
     * is invoked by the container if the underlying beanlet instance is to be
     * initialized. The post-construct method of the underlying beanlet instance 
     * is called by invoking {@code ctx.proceed}. Any exceptions thrown by the
     * that post-construct method are automatically thrown by this 
     * {@code proceed} invocation.
     * 
     * @param ctx provides access to the invocation context currently active.
     */
    @PostConstruct
    public void init(InvocationContext ctx) throws Exception {
        logger.info("Demo interceptor says: before-init");
        try {   
            ctx.proceed();
        } finally {
            logger.info("Demo interceptor says: after-init");
        }
    }
    
    /**
     * This method intercepts business methods for which it is configured.
     * If such business methods are invoked - either by container, or 
     * application - the container wraps this method around the call. The actual
     * business method is invoked as result of calling {@code ctx.proceed}. This
     * {@code proceed} method returns the result of the business method call, or
     * returns {@code null} in case of a {@code void} method. If the business
     * method throws an exception, it is propagated throuhh the {@code proceed}
     * method.
     * 
     * @param ctx provides access to the invocation context currently active.
     */
    @AroundInvoke
    public Object wrapMethod(InvocationContext ctx) throws Exception {
        logger.info("Demo interceptor says: before");
        try {
            return ctx.proceed();
        } finally {
            logger.info("Demo interceptor says: after");
        }
    }

    /**
     * This method interceptors the beanlet's pre-destroy method. This method
     * is invoked by the container if the underlying beanlet instance is to be
     * destroyed. The pre-destroy method of the underlying beanlet instance is
     * called by invoking {@code ctx.proceed}. Any exceptions thrown by the
     * that pre-destroy method are automatically thrown by this {@code proceed}
     * invocation.
     * 
     * @param ctx provides access to the invocation context currently active.
     */
    @PreDestroy
    public void destroy(InvocationContext ctx) throws Exception {
        logger.info("Demo interceptor says: before-destroy");
        try {   
            ctx.proceed();
        } finally {
            logger.info("Demo interceptor says: after-destroy");
        }
    }
}
