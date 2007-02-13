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

import java.util.List;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

import wicket.Component;
import wicket.application.IComponentInstantiationListener;
import wicket.protocol.http.WebApplication;

/**
 * Seasar管理下にあるコンポーネントをWicketコンポーネントが持つフィールドにインジェクションする処理を行うクラスです。
 * <p>このクラスが持つインジェクション処理は，Wicketコンポーネントがインスタンス化された際に行われます。
 * つまり，{@link IComponentInstantiationListener}の実装クラスとして提供します。</p>
 * <p>このクラスのオブジェクトの登録は，Applicationクラスの初期化処理の一環として実行します。</p>
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
 *             new SeasarComponentInjectionListener(this));
 *         ...
 *     }
 * 
 * }
 * </pre>
 * <p>上記の例では，Seasarコンテナが適切に存在することが前提条件となります。
 * つまり，S2ContainerServletなどでSeasarコンテナが準備され，
 * SingletonS2ContainerFactory.getContainer()メソッドによってSeasarコンテナが取得できる状態に
 * なっている必要があります。</p>
 * <p>インジェクション対象とするフィールドの判断基準を自作したい場合は，{@link FieldFilter}インタフェースの実装クラスを
 * 作成して，S2Wicketに登録する必要があります。この方法については，{@link FieldFilter}インタフェースの説明をご覧ください。</p>
 * <p>{@link SeasarComponentInjectionListener}オブジェクトのWicketへの登録は，Applicationクラス（のサブクラス）
 * のコンストラクタ内では記述しないでください。Applicationクラスのコンストラクタ内で登録を行うと，スレッドにApplicationオブジェクトが
 * アタッチされていないため，S2Wicket内で例外が発生します。init()メソッドをオーバーライドして，
 * その中で登録処理を記述してください。</p>
 * 
 * @see FieldFilter
 * @author Yoichiro Tanaka
 * @since 1.0.0
 */
public class SeasarComponentInjectionListener implements IComponentInstantiationListener {
	
	/** Seasarコンポーネントインジェクションプロセッサ */
	private InjectionProcessor injectionProcessor;
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。<br />
	 * <p>このコンストラクタでは，SingletonS2ContainerFactory.getContainer()メソッドによってSeasarコンテナが取得できる状態に
	 * なっている必要があります。</p>
	 * <p>このコンストラクタを利用した場合は，{@link AnnotationFieldFilter}フィールドフィルタと，Seasarコンテナに
	 * 登録されている{@link FieldFilter}インタフェースの実装オブジェクトが，フィールドフィルタとして適用されます。</p>
	 * @param application アプリケーションオブジェクト
	 */
	public SeasarComponentInjectionListener(WebApplication application) {
		this(application, SingletonS2ContainerFactory.getContainer(), null);
	}
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。<br />
	 * <p>このコンストラクタを使用することにより，手元にあるSeasarコンテナオブジェクトを，ルックアップの対象とすることができます。</p>
	 * <p>このコンストラクタを利用した場合は，{@link AnnotationFieldFilter}フィールドフィルタと，Seasarコンテナに
	 * 登録されている{@link FieldFilter}インタフェースの実装オブジェクトが，フィールドフィルタとして適用されます。</p>
	 * @param application アプリケーションオブジェクト
	 * @param container Seasarコンテナオブジェクト
	 */
	public SeasarComponentInjectionListener(WebApplication application, S2Container container) {
		this(application, container, null);
	}
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。<br />
	 * <p>このコンストラクタでは，SingletonS2ContainerFactory.getContainer()メソッドによってSeasarコンテナが取得できる状態に
	 * なっている必要があります。</p>
	 * <p>このコンストラクタを使用する場合は，引数に指定されたフィールドフィルタが適用されます。つまり，
	 * {@link AnnotationFieldFilter}オブジェクトやSeasarコンテナに登録された{@link FieldFilter}オブジェクトが
	 * 暗黙的に使用されることはありません。</p>
	 * @param application アプリケーションオブジェクト
	 * @param fieldFilters フィールドフィルタが格納されたコレクション
	 */
	public SeasarComponentInjectionListener(WebApplication application, List<FieldFilter> fieldFilters) {
		this(application, SingletonS2ContainerFactory.getContainer(), fieldFilters);
	}
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。<br />
	 * <p>このコンストラクタを使用することにより，手元にあるSeasarコンテナオブジェクトを，ルックアップの対象とすることができます。</p>
	 * <p>このコンストラクタを使用する場合は，引数に指定されたフィールドフィルタが適用されます。つまり，
	 * {@link AnnotationFieldFilter}オブジェクトやSeasarコンテナに登録された{@link FieldFilter}オブジェクトが
	 * 暗黙的に使用されることはありません。</p>
	 * @param application アプリケーションオブジェクト
	 * @param container Seasarコンテナオブジェクト
	 * @param fieldFilters フィールドフィルタが格納されたコレクション
	 */
	public SeasarComponentInjectionListener(
			WebApplication application, S2Container container, List<FieldFilter> fieldFilters) {
		super();
		// 引数チェック
		if (application == null)
			throw new IllegalArgumentException("application is null.");
		if (container == null)
			throw new IllegalArgumentException("container is null.");
		if ((fieldFilters != null) && (fieldFilters.size() == 0))
			throw new IllegalArgumentException("fieldFilters is empty.");
		// SeasarコンテナオブジェクトをWicketメタデータとして格納
		S2ContainerHolder.store(application, container);
		// インジェクションプロセッサを生成
		if (fieldFilters == null) {
			injectionProcessor = new InjectionProcessor(new S2ContainerLocator());
		} else {
			injectionProcessor = new InjectionProcessor(new S2ContainerLocator(), fieldFilters);
		}
	}

	/**
	 * コンポーネントがインスタンス化されたときに呼び出されます。<br />
	 * <p>ここでは，Seasarコンポーネントの呼び出しを行う動的プロキシのインジェクション処理を行います。</p>
	 * @param component 処理対象のコンポーネントオブジェクト
	 */
	public void onInstantiation(Component component) {
		// インジェクションを実行
		injectionProcessor.inject(component);
	}

}
