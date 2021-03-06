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
package org.beanlet;

/**
 * Specifies the wiring mode.
 *
 * @see Wiring
 * @author Leon van Zantvoort
 */
public enum WiringMode {

    /**
     * Wiring by info selects the object that is mapped to the name, inferred
     * from the target member, in the info map specified at 
     * {@link BeanletFactory#create(java.util.Map)}. Wiring by info is only
     * supported for non-static beanlets.
     */
    BY_INFO,
    
    /**
     * Wiring by name selects the beanlet that is registered under the name
     * inferred from the target member.
     */
    BY_NAME,

    /**
     * Wiring by type selects the beanlet that matches the type that is inferred 
     * from the target member. Wiring fails if multiple types have been found.
     */
    BY_TYPE
}
