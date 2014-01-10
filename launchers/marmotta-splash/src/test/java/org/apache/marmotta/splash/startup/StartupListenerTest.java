package org.apache.marmotta.splash.startup;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.marmotta.splash.common.MarmottaContext;

public class StartupListenerTest {
    
    private final StartupListener sl;
    private final MarmottaContext marmotta;

    public StartupListenerTest() {
        sl = new StartupListener();
        marmotta = new MarmottaContext();
    }
    
    public void testLifecycleEvent() {
        sl.lifecycleEvent(new LifecycleEvent(marmotta, Lifecycle.AFTER_START_EVENT, null));
    }

    public static void main(String[] args) {
        new StartupListenerTest().testLifecycleEvent();
    }
    
    
}
