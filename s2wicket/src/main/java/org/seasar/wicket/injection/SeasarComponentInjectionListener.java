/*
 * $Id$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.seasar.wicket.injection;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

import wicket.Component;
import wicket.application.IComponentInstantiationListener;
import wicket.protocol.http.WebApplication;

/**
 * Seasar管理下にあるコンポーネントをWicketコンポーネントが持つSeasarComponentアノテーションが
 * 付与されたフィールドにインジェクションする処理を行うクラスです。<br />
 * このクラスが持つインジェクション処理は，Wicketコンポーネントがインスタンス化された際に行われます。
 * つまり，{@link IComponentInstantiationListener}の実装クラスとして提供します。
 * このクラスのオブジェクトの登録は，Applicationクラスの初期化処理の一環として実行します。<br />
 * <pre>
 * public class OrderApplication extends WebApplication {
 * 
 *     public OrderApplication() {
 *         ...
 *         addComponentInstantiationListener(
 *             new SeasarComponentInjectionListener(this));
 *         ...
 *     }
 * 
 * }
 * </pre>
 * 上記の例では，Seasarコンテナが適切に存在することが前提条件となります。
 * つまり，S2ContainerServletなどでSeasarコンテナが準備され，
 * SingletonS2ContainerFactory.getContainer()メソッドによってSeasarコンテナが取得できる状態に
 * なっている必要があります。
 * 
 * @author Yoichiro Tanaka
 */
public class SeasarComponentInjectionListener implements IComponentInstantiationListener {
	
	/** Seasarコンポーネントインジェクションプロセッサ */
	private InjectionProcessor injectionProcessor;
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。<br />
	 * このコンストラクタでは，SingletonS2ContainerFactory.getContainer()メソッドによってSeasarコンテナが取得できる状態に
	 * なっている必要があります。
	 * @param application アプリケーションオブジェクト
	 */
	public SeasarComponentInjectionListener(WebApplication application) {
		this(application, SingletonS2ContainerFactory.getContainer());
	}
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 * @param application アプリケーションオブジェクト
	 * @param container Seasarコンテナオブジェクト
	 */
	public SeasarComponentInjectionListener(WebApplication application, S2Container container) {
		super();
		// 引数チェック
		if (application == null)
			throw new IllegalArgumentException("application is null.");
		if (container == null)
			throw new IllegalArgumentException("container is null.");
		// SeasarコンテナオブジェクトをWicketメタデータとして格納
		S2ContainerHolder.store(application, container);
		// インジェクションプロセッサを生成 */
		injectionProcessor = new InjectionProcessor(new S2ContainerLocator());
	}

	/**
	 * コンポーネントがインスタンス化されたときに呼び出されます。
	 * ここでは，Seasarコンポーネントの呼び出しを行う動的プロキシのインジェクション処理を行います。
	 * @param component 処理対象のコンポーネントオブジェクト
	 */
	public void onInstantiation(Component component) {
		// インジェクションを実行
		injectionProcessor.inject(component);
	}

}
