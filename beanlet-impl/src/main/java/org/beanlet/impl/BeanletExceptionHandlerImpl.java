package org.beanlet.impl;

import static org.beanlet.common.Beanlets.*;
import java.lang.reflect.Method;
import org.beanlet.BeanletApplicationException;
import org.beanlet.BeanletCreationException;
import org.beanlet.BeanletEventException;
import org.beanlet.BeanletEventNotExecutableException;
import org.beanlet.BeanletException;
import org.beanlet.BeanletExecutionException;
import org.beanlet.BeanletNotActiveException;
import org.beanlet.BeanletNotFoundException;
import org.beanlet.BeanletStateException;
import org.beanlet.Event;
import org.jargo.ComponentApplicationException;
import org.jargo.ComponentCreationException;
import org.jargo.ComponentEventException;
import org.jargo.ComponentEventNotExecutableException;
import org.jargo.ComponentException;
import org.jargo.ComponentExceptionHandler;
import org.jargo.ComponentExecutionException;
import org.jargo.ComponentNotActiveException;
import org.jargo.ComponentNotFoundException;
import org.jargo.ComponentStateException;

/**
 *
 * @author Leon van Zantvoort
 */
public final class BeanletExceptionHandlerImpl implements ComponentExceptionHandler {
    
    public void onException(Method method, ComponentException e) throws
            Throwable {
        try {
            throw e;
        } catch (ComponentExecutionException e2) {
            Throwable t = e2.getCause();
            assert t != null;
            // Check if exception is supported by throws clause of underlying method.
            boolean supported = t instanceof RuntimeException || t instanceof Error;
            if (!supported) {
                for (Class<?> cls : method.getExceptionTypes()) {
                    if (cls.isAssignableFrom(t.getClass())) {
                        supported = true;
                        break;
                    }
                }
            }
            
            if (supported) {
                throw t;
            } else {
                Object event = e2.getEvent();
                assert event instanceof Event;
                // Undeclared throwable.
                throw new BeanletExecutionException(e2.getComponentName(),
                        (Event) e2.getEvent(), t);
            }
        } catch (ComponentEventNotExecutableException e2) {
            Object event = e2.getEvent();
            assert event instanceof Event;
            throw new BeanletEventNotExecutableException(
                    e2.getComponentName(), (Event) event, 
                    CHAIN_JARGO_EXCEPTIONS ? e2 : e2.getCause());
        } catch (ComponentEventException e2) {
            Object event = e2.getEvent();
            assert event instanceof Event;
            throw new BeanletEventException(e2.getComponentName(), 
                    (Event) event, CHAIN_JARGO_EXCEPTIONS ? e2 : e2.getCause());
        } catch (ComponentNotActiveException e2) {
            throw new BeanletNotActiveException(e2.getComponentName(), 
                    CHAIN_JARGO_EXCEPTIONS ? e2 : e2.getCause());
        } catch (ComponentCreationException e2) {
            throw new BeanletCreationException(e2.getComponentName(), 
                    CHAIN_JARGO_EXCEPTIONS ? e2 : e2.getCause());
        } catch (ComponentNotFoundException e2) {
            throw new BeanletNotFoundException(e2.getComponentName(), 
                    CHAIN_JARGO_EXCEPTIONS ? e2 : e2.getCause());
        } catch (ComponentStateException e2) {
            throw new BeanletStateException(e2.getComponentName(), 
                    CHAIN_JARGO_EXCEPTIONS ? e2 : e2.getCause());
        } catch (ComponentException e2) {
            throw new BeanletException(e2.getComponentName(), 
                    CHAIN_JARGO_EXCEPTIONS ? e2 : e2.getCause());
        } catch (ComponentApplicationException e2) {
            throw new BeanletApplicationException(
                    CHAIN_JARGO_EXCEPTIONS ? e2 : e2.getCause());
        }
    }
}
