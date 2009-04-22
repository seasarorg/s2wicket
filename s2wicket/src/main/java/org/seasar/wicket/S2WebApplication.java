package org.seasar.wicket;

import org.apache.wicket.protocol.http.HttpSessionStore;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.session.ISessionStore;

public abstract class S2WebApplication extends WebApplication {
    @Override
    protected ISessionStore newSessionStore() {
        if (DEPLOYMENT.equalsIgnoreCase(getConfigurationType())) {
            return super.newSessionStore();
        } else {
            return new HttpSessionStore(this);
        }
    }
}
