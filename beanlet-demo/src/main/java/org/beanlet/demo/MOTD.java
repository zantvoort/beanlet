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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;
import org.beanlet.Inject;
import org.beanlet.Schedule;

/**
 * The {@code MOTD} class submits messages to the logging system. The class is 
 * not annotated with any {@code ScopeAnnotation}s, which means that it is a 
 * vanilla beanlet. Vanilla beanlets are singleton by default. Only one instance 
 * of this beanlet exists at the same time.</p>
 * 
 * @author Leon van Zantvoort
 */
public final class MOTD {

    private final URL url;
    private final Logger logger;
    
    //
    // <DEPENDENCY-INJECTION>
    //
    // The following arguments are injected by the container during beanlet
    // instance creation. Dependency injection takes place just before any
    // lifecycle- or business-methods are invoked on the beanlet instance.
    //
    
    /**
     * This constructor is subject to constructor injection. 
     * 
     * @param url url that represents the location of the MOTD. The container
     * requires that an injectant is defined in the beanlet.xml file.
     * @param logger logger instance, which is injected automatically by the
     * container.
     */
    public MOTD(
            @Inject URL url, 
            @Inject Logger logger) {
        this.url = url;
        this.logger = logger;
    }
    
    //
    // </DEPENDENCY-INJECTION>
    // <BUSINESS-METHODS>
    //
    
    /**
     * Reads the MOTD from the specified {@code url} and writes it to the
     * injected {@code logger}. This method is annotated with the 
     * {@code Schedule} annotation. This annotation specifies that this method
     * is to be invoked by the container 10 seconds after it has been deployed.
     * This method is called by a container thread outside the context of 
     * deployment / beanlet creation.
     */
    @Schedule(once=true, initialDelay=10000)
    public void printMotd() throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    url.openStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
            }
        } catch (IOException e) {
            logger.warning("Unable to retrieve MOTD from: " + url + ".");
            throw e;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    //
    // </BUSINESS-METHODS>
    //
}
