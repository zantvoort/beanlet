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

import static org.beanlet.management.ManageableElementType.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a beanlet with this annotation to expose it to JMX.
 *
 * {@beanlet.annotation}
 *
 * @author Leon van Zantvoort
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Manageable {
    
    /**
     * Specifies a {@code NamingStrategy} implementation that is responsible
     * for generating the MBean's {@code ObjectName}. The specified class must
     * have a zero-args constructor.
     */
    Class<? extends NamingStrategy> namingStrategy() default 
            IdentityNamingStrategy.class;
    
    /**
     * Specifies which registration policy must be applied.
     */
    RegistrationPolicy registrationPolicy() default 
            RegistrationPolicy.FAIL_ON_EXISTING;

    /**
     * Specifies which element types must be exposed.
     */
    ManageableElementType[] exposed() default {ATTRIBUTE, OPERATION};
}
