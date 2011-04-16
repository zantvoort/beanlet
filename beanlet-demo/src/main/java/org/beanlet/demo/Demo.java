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

import static javax.persistence.PersistenceContextType.*;
import static org.beanlet.transaction.TransactionAttributeType.*;
import static org.beanlet.RetentionPolicy.*;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.beanlet.Inject;
import org.beanlet.PostConstruct;
import org.beanlet.PreDestroy;
import org.beanlet.Retention;
import org.beanlet.Start;
import org.beanlet.Stateful;
import org.beanlet.Stop;
import org.beanlet.demo.entity.DemoSession;
import org.beanlet.transaction.TransactionAttribute;

/**
 * <p>This demo beanlet is responsible for logging each time this demo runs.
 * It also writes some information on previous demo sessions to the logging
 * system.</p>
 * 
 * <p>This is a transactional stateful beanlet, which means that all methods 
 * called on the beanlet are delegated to the same beanlet instance. This 
 * beanlet also demonstrates the EJB3-style extended persistence context. This 
 * means that the persistence context goes beyond the transaction scope.</p>
 * 
 * <p>The {@code Retention(REMOVE)} annotation is applied to remove a beanlet
 * instance if a business method results in an unchecked exception, preventing
 * that any more methods are invoked on that same instance.</p>
 * 
 * <p>Stateful beanlets are non-reentrant by default. This behavior can be
 * changed by setting {@code Stateful#reentrant} to {@code true}.</p>
 * 
 * @author Leon van Zantvoort
 */
@TransactionAttribute(REQUIRED)
@Retention(REMOVE)
@Stateful
public class Demo {
    
    private DemoSession session;
    
    //
    // <DEPENDENCY-INJECTION>
    //
    // The following fields are injected by the container right after beanlet
    // instance creation. Dependency injection takes place just before any
    // lifecycle- or business-methods are invoked on the beanlet instance.
    //
    
    /**
     * The container injects an entity manager that runs within the scope of an
     * extended persistence context.
     */
    @PersistenceContext(unitName="jta-demo", type=EXTENDED)
    private EntityManager em;
    
    /**
     * This logger instance is automatically injected by the container.
     */
    @Inject
    private Logger logger;
    
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
        logger.info("Demo beanlet says: init");
    }
    
    /**
     * <p>The beanlet container detects - during deployment - that this beanlet 
     * defines a {@code Start} method. As a result, the container creates an
     * instance for this beanlet, performs dependency injection and invokes the 
     * {@code Start} method on it. This method is invoked within the context of 
     * deployment / beanlet creation.</p>
     */
    @Start
    public void start() {
        session = new DemoSession();
        session.setStartTimestamp(new Date());
    }
    
    /**
     * <p>The beanlet container detects - during undeployment - that this 
     * beanlet defines a {@code Stop} method. As a result, the container calls
     * this method just before invoking the pre-destroy method. This method
     * is invoked within the context of undeployment / beanlet removal.</p>
     */
    @Stop
    public void stop() {
        logger.info("Writing session to database.");
        session.setEndTimestamp(new Date());
        em.persist(session);
    }

    /**
     * The pre-destroy method is invoked by the container just before the
     * beanlet instance is being discarded. This method is invoked within the 
     * context of undeployment / beanlet removal.
     */
    @PreDestroy
    public void destroy() {
        logger.info("Demo beanlet says: destroy");
    }
    
    //
    // </LIFECYCLE-METHODS>
    // <BUSINESS-METHODS>
    //
    
    /**
     * <p>Reads all recorded demo sessions from the database and writes their 
     * details to the logging system.</p>
     * 
     * <p>Without the annotations expressed in the beanlet xml-file, this method
     * wouldn't be a business method (stateful beanlets do not expose any
     * methods by default). The xml-file declares that this beanlet exposes
     * the Runnable interface. Obviously, this class does not expose a 
     * zero-argument run method. The xml-file therefore marks this method with
     * the {@code operation} annotation so that this method can be mapped to
     * the Runnable interface's {@code run} method.</p>
     * 
     * <p>Note that the beanlet would have failed to be defined without this 
     * mapped method.</p>
     */
    public void doIt() {
        Query query = em.createQuery(
                "SELECT s FROM DemoSession s ORDER BY s.sessionId ASC");
        List resultList = query.getResultList();
        logger.info("");
        logger.info("This demo uses the Java Persistence API to log each");
        logger.info("time the demo has been started.");
        if  (resultList.isEmpty()) {
            logger.info("");
            logger.info("No previous demo sessions have been found in the");
            logger.info("database.");
        } else {
            logger.info("");
            for (Object o : resultList) {
                DemoSession ds = (DemoSession) o;
                logger.info(String.valueOf(ds));
            }
        }
        logger.info("");
        logger.info("Note that this beanlet is implemented in such a way");
        logger.info("that it only commits a demo session to the database");
        logger.info("while invoking the stop lifecycle method of this");
        logger.info("beanlet. This can be enforced by undeploying this");
        logger.info("component (remove jar-file from deploy directory)");
        logger.info("or by shutting down the container gracefully (ctrl-c).");
        logger.info("");
        logger.info("Please wait a few more seconds, because some other");
        logger.info("beanlets would like to show off as well.");
        logger.info("");
    }

    //
    // </BUSINESS-METHODS>
    //
}
