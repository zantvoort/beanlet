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
 * <p>Thrown to indicate a general application error. All beanlet related 
 * exceptions subclass this exception.</p>
 *
 * @author Leon van Zantvoort
 */
public class BeanletApplicationException extends RuntimeException {
    
    private static final long serialVersionUID = 2352353252352353253L;

    public BeanletApplicationException() {
        super();
    }
    
    public BeanletApplicationException(String message) {
        super(message);
    }

    public BeanletApplicationException(Throwable cause) {
        super(cause);
    }
    
    public BeanletApplicationException(String message, 
            Throwable cause) {
        super(message, cause);
    }
}
