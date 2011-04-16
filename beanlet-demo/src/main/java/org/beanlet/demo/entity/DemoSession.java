package org.beanlet.demo.entity;

import static javax.persistence.GenerationType.*;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

/**
 * This class represents a JPA entity. This class lacks documentation as it is
 * fully JPA specific.in now way beanlet specific.
 * 
 * @author Leon van Zantvoort
 */
@Entity
@TableGenerator(name="DEMO_SEQ", allocationSize=1)
public class DemoSession implements Serializable {
    
    private static final long serialVersionUID = 23512351322325L;
    
    private long sessionId;
    private Date startTimestamp;
    private Date endTimestamp;

    public DemoSession() {
    }

    @Id
    @GeneratedValue(strategy=TABLE, generator="DEMO_SEQ")
    public long getSessionId() {
        return sessionId;
    }

    /**
     * Sets some session identifier.
     */
    public void setSessionId(long activationId) {
        this.sessionId = activationId;
    }

    /**
     * Returns the start timestamp.
     */
    public Date getStartTimestamp() {
        return startTimestamp;
    }

    /**
     * Sets the start timestamp.
     */
    public void setStartTimestamp(Date start) {
        this.startTimestamp = start;
    }

    /**
     * Returns the end timestamp.
     */
    public Date getEndTimestamp() {
        return endTimestamp;
    }

    /**
     * Sets the end timestamp.
     */
    public void setEndTimestamp(Date end) {
        this.endTimestamp = end;
    }

    /**
     * Overrides the default {@code toString} implementation to print a more
     * human friendly string.
     */
    @Override
    public String toString() {
        return "Session: " + getSessionId() + ", start: " + getStartTimestamp() +
                ", end: " + getEndTimestamp();
    }
}
