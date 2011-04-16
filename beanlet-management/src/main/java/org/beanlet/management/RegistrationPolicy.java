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
package org.beanlet.management;

/**
 * Specifies three different registration policies.
 *
 * @author Leon van Zantvoort
 */
public enum RegistrationPolicy {
    
    /**
     * Registration must fail if an MBean is already registered under the
     * specified {@code ObjectName}.
     */
    FAIL_ON_EXISTING,
    
    /**
     * Replace a previously registered MBean if it was already registered under
     * the specified {@code ObjectName}.
     */
    REPLACE_EXISTING,
    
    /**
     * Ignore if any previously registered MBeans match the specified 
     * {@code ObjectName}.
     */
    IGNORE_EXISTING
}
