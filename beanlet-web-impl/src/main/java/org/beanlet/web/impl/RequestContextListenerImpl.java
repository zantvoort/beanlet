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

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author Leon van Zantvoort
 */
public final class RequestContextListenerImpl implements ServletRequestListener {

    private final ServletRequestListener listener;

    public RequestContextListenerImpl() {
        if (!WebConstants.isWebServletSupported()) {
            BeanletApplicationContext.instance();
            this.listener =  new RequestContextListener();
        } else {
            this.listener = new ServletRequestListener() {
                public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
                }

                public void requestInitialized(ServletRequestEvent servletRequestEvent) {
                }
            };
        }
    }

    public void requestDestroyed(ServletRequestEvent event) {
        listener.requestDestroyed(event);
    }

    public void requestInitialized(ServletRequestEvent event) {
        listener.requestInitialized(event);
    }
}
