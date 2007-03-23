package org.seasar.wicket.uifactory;

import wicket.Component;
import wicket.application.IComponentInstantiationListener;

public class WicketComponentBuildingListener implements IComponentInstantiationListener {

	/** Wicketコンポーネントビルディングプロセッサ */
	private BuildingProcessor buildingProcessor;
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 */
	public WicketComponentBuildingListener() {
		super();
		// ビルディングプロセッサを生成
		buildingProcessor = new BuildingProcessor();
	}

	/**
	 * コンポーネントがインスタンス化されたときに呼び出されます。<br />
	 * <p>ここでは，Wicketコンポーネントのビルディング処理を行います。</p>
	 * @param component 処理対象のコンポーネントオブジェクト
	 */
	public void onInstantiation(Component component) {
		// ビルディングプロセッサに処理を委譲
		buildingProcessor.build(component);
	}

}
