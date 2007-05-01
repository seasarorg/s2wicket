package org.seasar.wicket.uifactory;

import wicket.Application;
import wicket.IRequestCycleFactory;
import wicket.Session;

public class DummySession extends Session {

	protected DummySession(Application application) {
		super(application);
	}

	@Override
	protected IRequestCycleFactory getRequestCycleFactory() {
		return null;
	}

}
