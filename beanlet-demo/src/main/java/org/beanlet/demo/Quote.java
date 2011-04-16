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

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.beanlet.Inject;
import org.beanlet.PostConstruct;
import org.beanlet.Schedule;
import org.beanlet.Stateless;

/**
 * <p>This class writes quotes to the logging system. The class is marked with
 * the {@code Stateless} annotation. The container is responsible for 
 * maintaining a pool of stateless beanlet instances. Calls to this beanlet are
 * delegated to one of the available beanlet instances.</p>
 * 
 * <p>Stateless beanlets are non-reentrant by default. This behavior can be
 * changed by setting {@code Stateless#reentrant} to {@code true}.</p>
 *
 * @author Leon van Zantvoort
 */
@Stateless
public class Quote {
    
    private Iterator<String> iterator;
    private List<String> quotes;

    //
    // <DEPENDENCY-INJECTION>
    //
    // The following field is injected by the container right after beanlet
    // instance creation. Dependency injection takes place just before any
    // lifecycle methods are invoked on the beanlet instance.
    //
    
    /**
     * This logger instance is automatically injected by the container.
     */
    @Inject
    private Logger logger;
    
    /**
     * Sets the a list of quotes that are to be 
     */
    @Inject
    public void setQuotes(List<String> messages) {
        this.quotes = messages;
    }

    //
    // </DEPENDENCY-INJECTION>
    // <LIFECYCLE-METHODS>
    //
    
    /**
     * The post-construct method is invoked by the container right after 
     * dependency injection. This method is invoked within the context of
     * deployment / beanlet creation.
     */
    @PostConstruct
    public void init() {
        iterator = quotes.iterator();
    }
    
    //
    // </LIFECYCLE-METHODS>
    // <BUSINESS-METHODS>
    //
    
    /**
     * Returns the list of quotes that are configured for this class.
     */
    public List<String> getQuotes() {
        return quotes;
    }
    
    /**
     * <p>Writes one of the configured {@code quotes} to the logger. This method 
     * is invoked by the container every 5 whole seconds starting (at least)
     * 20 seconds after deployment of this beanlet.</p>
     * 
     * <p>This method is called by a container thread outside the context of 
     * deployment / beanlet creation.</p>
     */
    @Schedule(initialDelay=20000, cron="0/5 * * * * *")
    public void print() {
        if (iterator.hasNext()) {
            logger.info(iterator.next());
        } else {
            iterator = quotes.iterator();
            if (iterator.hasNext()) {
                logger.info(iterator.next());
            }
        }
    }

    //
    // </BUSINESS-METHODS>
    //
}
