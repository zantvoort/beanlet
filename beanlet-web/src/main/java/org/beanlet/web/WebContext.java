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
package org.beanlet.web;

import org.beanlet.BeanletStateException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Provides access to the web runtime context of the beanlet instance.
 *
 * @author Leon van Zantvoort
 */
public interface WebContext {

    /**
     * Returns the current <code>ServletRequest</code>.
     * @throws BeanletStateException
     * @throws org.beanlet.BeanletStateException if the current thread is not associated
     * with a servlet call.
     */
    ServletRequest getServletRequest() throws BeanletStateException;
}
