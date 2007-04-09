package org.seasar.wicket.uifactory;

import wicket.Application;
import wicket.ISessionFactory;
import wicket.session.ISessionStore;

public class DummyApplication extends Application {

	public DummyApplication() {
		super();
		// TODO Auto-generated constructor stub
		super.internalInit();
		super.init();
	}

	@Override
	public String getApplicationKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class getHomePage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ISessionFactory getSessionFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ISessionStore newSessionStore() {
		// TODO Auto-generated method stub
		return null;
	}

}
