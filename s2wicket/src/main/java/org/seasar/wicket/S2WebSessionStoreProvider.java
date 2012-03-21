package org.seasar.wicket;

import org.apache.wicket.Application;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.session.HttpSessionStore;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.IProvider;

public class S2WebSessionStoreProvider implements IProvider<ISessionStore> {
    private final IProvider<ISessionStore> defaultProvider;
    private final RuntimeConfigurationType configurationType;

    public S2WebSessionStoreProvider(Application application) {
        this.defaultProvider = application.getSessionStoreProvider();
        this.configurationType = application.getConfigurationType();
    }

    public ISessionStore get() {
        if (configurationType == RuntimeConfigurationType.DEPLOYMENT) {
            return defaultProvider.get();
        } else {
            return new HttpSessionStore();
        }
    }
}
