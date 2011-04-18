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
package org.beanlet.web.impl;

import org.beanlet.BeanletApplicationContext;

import java.util.IdentityHashMap;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

/**
 *
 * @author Leon van Zantvoort
 */
public class RequestContextListenerImpl implements ServletRequestListener {

    private static final String SESSION_DESTROY_HOOK_NAME =
            RequestContextListenerImpl.class.getName() + ".SESSION_DESTROY_HOOK";
    
    private static final ThreadLocal<HttpServletRequest> local = 
            new ThreadLocal<HttpServletRequest>();
    
    private static final Map<HttpServletRequest, Runnable> hooks =
            new IdentityHashMap<HttpServletRequest, Runnable>();
    
    public RequestContextListenerImpl() {
        BeanletApplicationContext.instance();
    }

    public void requestDestroyed(ServletRequestEvent event) {
        ServletRequest request = event.getServletRequest();
        assert request == get();
        local.remove();
        final Runnable runnable;
        synchronized (hooks) {
            runnable = hooks.remove(request);
        }
        if (runnable != null) {
            runnable.run();
        }
    }

    public void requestInitialized(ServletRequestEvent event) {
        ServletRequest request = event.getServletRequest();
        if (request instanceof HttpServletRequest) {
            local.set((HttpServletRequest) request);
        }
    }
    
    public static HttpServletRequest get() {
        return local.get();
    }
    
    /**
     * @throws IllegalStateException if no request is active.
     */
    public static void setRequestDestroyHook(Runnable runnable) {
        HttpServletRequest request = get();
        if (request == null) {
            throw new IllegalStateException();
        }
        synchronized (hooks) {
            hooks.put(request, runnable);
        }
    }

    /**
     * @throws IllegalStateException if no request is active.
     */
    public static void setSessionDestroyHook(final Runnable runnable) {
        HttpServletRequest request = get();
        if (request == null) {
            throw new IllegalStateException();
        }
        request.getSession().setAttribute(SESSION_DESTROY_HOOK_NAME, 
                new HttpSessionBindingListener() {
            public void valueBound(HttpSessionBindingEvent event) {
            }
            public void valueUnbound(HttpSessionBindingEvent event) {
                runnable.run();
            }
        });
    }
}
