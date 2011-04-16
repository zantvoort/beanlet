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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;


/**
 * Represents a collection of values.
 *
 * @see org.beanlet.Inject
 * @author Leon van Zantvoort
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface CollectionValue {
  
    /**
     * Specifies the collection type that is to be constructed. This value must
     * be set to a concrete class, or to the {@code Collection} interface, in
     * which case the collection type is inferred from the member to be 
     * injected.
     */
    Class<? extends Collection> type() default Collection.class;
    
    /**
     * Contains all collection values.
     */
    Value[] value() default {};

    /**
     * {@code true} if annotation represents an empty collection.
     */
    boolean empty() default false;
    
    /**
     * {@code true} if the collection is unmodifiable after injection.
     */
    boolean unmodifiable() default false;
    
    /**
     * {@code true} if the collection is synchronized.
     */
    boolean synced() default false;
}
