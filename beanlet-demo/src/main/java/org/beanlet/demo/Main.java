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
package org.beanlet.demo;

import static org.beanlet.WiringMode.*;
import org.beanlet.Inject;
import org.beanlet.Start;
import org.beanlet.Wiring;

/**
 * <p>This class starts the beanlet demo. The class is not annotated with any
 * {@code ScopeAnnotation}s, which means that it is a vanilla beanlet. Vanilla 
 * beanlets are singleton by default. Only one instance of this beanlet exists 
 * at the same time.</p>
 * 
 * @author Leon van Zantvoort
 */
public class Main {
    
    //
    // <DEPENDENCY-INJECTION>
    //
    // The following field is injected by the container right after beanlet
    // instance creation. Dependency injection takes place just before any
    // lifecycle- or business-methods are invoked on the beanlet instance.
    //
    
    /**
     * This field is being injected as part of the beanlet-instance-creation
     * process. This field is required to be injected, b/c the 
     * {@code Inject.optional} specifies {@code false} by default. In other 
     * words, the beanlet instance fails to be created if no suitable injectant 
     * can be found.
     * 
     * The field is also marked with the {@code Wiring} annotation. This means 
     * that one of the three implicit wiring methods is being applied. In this
     * particular case wiring {@code BY_NAME} is selected. The container looks
     * if a beanlet that matches the name that can be inferred from the field.
     * If a beanlet is registered for that name, an instance of that specific 
     * beanlet is injected. 
     * This type of dependency injection is also referred to as implicit wiring, 
     * b/c the injectant is magically selected. Check the javadoc of 
     * {@code Inject} and {@code Wiring} for more information.
     */
    @Wiring(BY_NAME)
    @Inject
    private Runnable demo;
    
    //
    // </DEPENDENCY-INJECTION>
    // <LIFECYCLE-METHODS>
    //
    // Lifecycle init-methods are invoked by the container right after
    // dependency injection.
    //

    /**
     * <p>This method starts the demo by invoking {@code run} on the 
     * {@code demo} instance. This {@code demo} instance is guaranteed to be 
     * initialized as it is marked with the {@code Inject} annotation.</p>
     * 
     * <p>The beanlet container detects - during deployment - that this beanlet 
     * defines a {@code Start} method. As a result, the container creates an
     * instance for this beanlet, performs dependency injection and invokes the
     * {@code Start} method on it. This method is invoked within the context of 
     * deployment / initialization.</p>
     */
    @Start
    public void startDemo() {
        demo.run();
    }

    //    
    // </LIFECYCLE-METHODS>
    //
}
