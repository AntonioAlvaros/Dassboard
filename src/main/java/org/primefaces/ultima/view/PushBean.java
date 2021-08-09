/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.primefaces.ultima.view;
/**
 *
 * @author kerwin
 */
    import java.io.Serializable;
import java.text.Annotation;
    import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.PostConstruct;

    import javax.inject.Inject;
    import javax.inject.Named;

    import org.omnifaces.cdi.Push;
    import org.omnifaces.cdi.PushContext;
    import org.omnifaces.cdi.ViewScoped;

    @Named
    @ViewScoped
        public  class PushBean implements Serializable {

        private static AtomicLong counter = new AtomicLong();

        private boolean connected;

        @Inject @Push(channel="counter")
        private PushContext push;

        public void toggle() {
            connected = !connected;
        }

        public void increment() {
            long newvalue = counter.incrementAndGet();
            
            push.send(newvalue);
        }

        public boolean isConnected() {
            return connected;
        }

        public Long getCount() {
            return counter.get();
        }

    }