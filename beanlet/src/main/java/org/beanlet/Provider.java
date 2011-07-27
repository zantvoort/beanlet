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
 * <p>
 * Provides instances of <code>T</code>. Typically implemented by an injector. For
 * any type <code>T</code> that can be injected, you can also inject
 * <code>Provider&lt;T&gt;</code>. Compared to injecting <code>T</code> directly, injecting
 * <code>Provider&lt;T&gt;</code> enables:
 *
 *  <ul>
 *  <li>retrieving multiple instances.</li>
 *  <li>lazy or optional retrieval of an instance.</li>
 *  <li>breaking circular dependencies.</li>
 *  <li>abstracting scope so you can look up an instance in a smaller scope
 *     from an instance in a containing scope.</li>
 * </ul>
 *
 * <p>For example:
 *
 * <pre>
 *  class Car {
 *    &#064;Inject Car(Provider&lt;Seat> seatProvider) {
 *      Seat driver = seatProvider.get();
 *      Seat passenger = seatProvider.get();
 *      ...
 *    }
 *  }</pre>
 * <P>
 *
 * @author Leon van Zantvoort
 */
public interface Provider<T> {


    /**
     * Provides a fully-constructed and injected instance of {@code T}
     */
    T get();
}
