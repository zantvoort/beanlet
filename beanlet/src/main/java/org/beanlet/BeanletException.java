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
 * Indicates that there is an issue with the specified beanlet. This exception
 * can be subclassed to express more detailed information.
 *
 * @author Leon van Zantvoort
 */
public class BeanletException extends BeanletApplicationException {
    
    private static final long serialVersionUID = 6010911652432861443L;
    
    private final String beanletName;
    
    public BeanletException(String beanletName) {
        super("'" + beanletName.toString() + "'");
        this.beanletName = beanletName;
    }

    public BeanletException(String beanletName, String message) {
        super("'" + beanletName.toString() + "': " + message.toString());
        this.beanletName = beanletName;
    }

    public BeanletException(String beanletName, Throwable cause) {
        super("'" + beanletName.toString() + "'", cause);
        this.beanletName = beanletName;
    }
    
    public BeanletException(String beanletName, String message, 
            Throwable cause) {
        super("'" + beanletName.toString() + "': " + message.toString(), cause);
        this.beanletName = beanletName;
    }
    
    public String getBeanletName() {
        return beanletName;
    }
}
