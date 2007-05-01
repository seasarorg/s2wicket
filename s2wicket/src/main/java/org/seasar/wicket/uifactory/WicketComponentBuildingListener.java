/*
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

package org.seasar.wicket.uifactory;

import org.seasar.wicket.injection.SeasarComponentInjectionListener;

import wicket.Application;
import wicket.Component;
import wicket.application.IComponentInstantiationListener;
import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.panel.Panel;

/**
 * S2Wicketが提供するUIコンポーネント構築機能を利用可能にするためのクラスです。<br />
 * <p>このクラスによって利用可能となるUIコンポーネントの構築処理は，Wicketコンポーネントがインスタンス化された際に行われます。
 * つまり，{@link IComponentInstantiationListener}の実装クラスとして提供します。</p>
 * <p>このクラスのオブジェクトの登録は，{@link Application}クラスの初期化処理の一環として実行します。</p>
 * <pre>
 * public class OrderApplication extends WebApplication {
 * 
 *     public OrderApplication() {
 *         ...
 *     }
 *     
 *     &#064;Override
 *     protected void init() {
 *         super.init();
 *         ...
 *         addComponentInstantiationListener(
 *             new WicketComponentBuildingListener(this));
 *         ...
 *     }
 * 
 * }
 * </pre>
 * <p>{@link WicketComponentBuildingListener}オブジェクトが{@link Application}オブジェクトに登録された後に，
 * {@link WebPage}クラスや{@link Panel}クラス，{@link Form}クラスを継承したアプリケーションクラスのなかで，
 * {@link WicketComponent}アノテーションや{@link WicketModel}アノテーションを使用することができるようになります。
 * 各Wicketコンポーネントがインスタンス化された際に，S2Wicketは各フィールドに付与されたアノテーションに基づいて，
 * コンポーネントオブジェクトの生成，モデルオブジェクトの生成，親のコンテナコンポーネントへの登録処理を行います。
 * 特にコンポーネントオブジェクトは，そのコンポーネントを継承した動的プロキシが自動生成され，
 * イベント処理などを{@link WicketAction}アノテーションにより記述されたOGNL式の評価で実現することが可能となります。</p>
 * 個々の処理については，各アノテーションのJavadocをご覧ください。</p>
 * <p>構築されるUIコンポーネントオブジェクトの生成や関連付けられるモデルオブジェクトの生成処理について，
 * Seasarコンテナが提供するコンポーネントの処理を呼び出したい場合は，{@link WicketComponentBuildingListener}オブジェクトの
 * {@link Application}オブジェクトへの登録の前に，{@link SeasarComponentInjectionListener}オブジェクトを
 * {@link Application}オブジェクトに登録しておく必要があります。</p>
 * <p>{@link WicketComponentBuildingListener}オブジェクトのWicketへの登録は，Applicationクラス（のサブクラス）
 * のコンストラクタ内では記述しないでください。Applicationクラスのコンストラクタ内で登録を行うと，スレッドにApplicationオブジェクトが
 * アタッチされていないため，S2Wicket内で例外が発生します。init()メソッドをオーバーライドして，
 * その中で登録処理を記述してください。</p>
 * 
 * @see WicketComponent
 * @see WicketModel
 * @see WicketAction
 * @see SeasarComponentInjectionListener
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
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
