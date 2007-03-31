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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import wicket.Component;
import wicket.model.BoundCompoundPropertyModel;
import wicket.model.CompoundPropertyModel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.model.PropertyModel;

/**
 * Wicketモデルフィールドの生成をS2Wicketに指示することを示すアノテーションです。<br />
 * <p>Wicketで提供されるコンポーネントは，何らかのモデルオブジェクトと関連付けられます。
 * コンポーネントは，イベントの発生などの事象により，モデルオブジェクトの内容を取得あるいは操作を行います。
 * コンポーネントとモデルオブジェクトが関連付けられるときには，モデルオブジェクトをそのままコンポーネントの
 * 生成時に渡してしまう手段もありますが，多くの場合は関連付けを行うための{@link IModel}オブジェクト
 * でモデルオブジェクトをラップしてコンポーネントのコンストラクタに渡します。{@link IModel}クラスの
 * 実装クラスとして，以下のクラスがWicketに標準で搭載されています。</p>
 * <ul>
 * <li>{@link Model} - 基本的なモデルを表す実装クラス。</li>
 * <li>{@link PropertyModel} - プロパティ名コンポーネントと関連付けを実現する実装クラス。</li>
 * <li>{@link CompoundPropertyModel} - wicket:idをプロパティ名として複数の子コンポーネントとの関連付けを実現する実装クラス。</li>
 * <li>{@link BoundCompoundPropertyModel} - {@link CompoundPropertyModel}に対して，明示的なプロパティ名の指定で関連付けを可能にする実装クラス。</li>
 * </ul>
 * <p>{@link WicketModel}アノテーションは，それが付与されたフィールドをモデルフィールドとしてマーキングします。
 * {@link WicketComponent}アノテーションによって指定されたコンポーネントフィールドの処理の際に，関連付けの
 * 必要があるモデルオブジェクトが，{@link WicketModel}アノテーションが付与されたモデルフィールドから取得されます。
 * この際，{@link #type()}属性で指定された種別に従ってモデルオブジェクトが適切なオブジェクトにラップされて，
 * コンポーネントに関連付けられます。どのようにモデルオブジェクトがコンポーネントと関連付けられるかは，
 * {@link ModelType}クラスのJavadocをご覧ください。</p>
 * <p>{@link WicketModel}アノテーションが付与されたフィールドにセットするオブジェクトの生成は，以下の手順で行われます。</p>
 * <ol>
 * <li>「create+[フィールド名]」という命名規則に基づいて命名された名前を持つメソッドが定義されていた場合は，
 * そのメソッドを呼び出して，その結果の戻り値をモデルオブジェクトとしてフィールドにセットする。</li>
 * <li>{@link #exp()}属性が指定されていた場合は，その属性値として書かれたOGNL式を評価し，
 * その結果得られた値をモデルオブジェクトとしてフィールドにセットする。</li>
 * <li>フィールドの型のデフォルトコンストラクタを使用してインスタンスを生成し，フィールドにセットする。</li>
 * </ol>
 * <p>最初の方法は，モデルオブジェクトの生成処理が複雑な手順になるなど，開発者が明示的に記述したい場合に適用します。
 * 下記のリストは明示的にモデルオブジェクトの生成メソッドを定義した例です。</p>
 * <pre>
 * &#064;WicketModel
 * private ConditionModel conditionModel;
 * public ConditionModel createConditionModel() {
 *     ...
 * }
 * </pre>
 * <p>定義するメソッドは，引数がなく，戻り値がモデルフィールドに代入する際に問題とならない型である必要があります。</p>
 * <p>createメソッドが定義されていない場合は，{@link #exp()}属性で指定されたOGNL式を評価することによって，
 * その評価結果がフィールドにセットされます。下記のリストは，MasterDataProducerクラスのstaticメソッドを
 * 呼び出してモデルオブジェクトとする例です。</p>
 * <pre>
 * &#064;WicketModel(type=ModelType.RAW
 *     exp="&#064;MasterDataProcuder&#064;getDivisionList()")
 * private List divisionList;
 * </pre>
 * <p>createメソッドもなく，{@link #exp()}属性も指定されていなかった場合は，
 * フィールドの型のデフォルトコンストラクタを使ってS2Wicketが暗黙的にインスタンスを生成し，
 * フィールドにセットします。</p>
 * <pre>
 * &#064;WicketModel
 * private ConditionModel conditionModel;
 * </pre>
 * <p>上記のリストでは，「new ConditionModel();」の結果がフィールドにセットされます。</p>
 * <p>{@link WicketModel}アノテーションに対する処理は，{@link Component}クラスの
 * デフォルトコンストラクタから呼び出されます。よって，{@link WicketModel}アノテーションが
 * 付与されたフィールドが定義されたクラスの他のフィールドについて，右辺の式が実行される前
 * となります。つまり，</p>
 * <pre>
 * &#064;WicketModel(exp="getConditionModel()")
 * private ConditionModel conditionModel = new ConditionModel();
 * </pre>
 * という記述を行うと，getConditionModel()メソッドの評価結果がS2Wicketによって
 * conditionModelフィールドにセットされますが，その後フィールドの右辺の式が実行され，
 * confitionModelフィールドの値が上書きされますので，注意が必要です。</p>
 * 
 * @see WicketComponent
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface WicketModel {

	/**
	 * モデルの種別を指定するための属性（任意）です。
	 * この属性を省略した場合，{@link ModelType#PROPERTY}が適用されます。
	 * @return モデルの種別
	 */
	public ModelType type() default ModelType.PROPERTY;
	
	/**
	 * モデルオブジェクトを生成するOGNL式を指定するための属性（任意）です。
	 * この属性を省略した場合，createメソッドの実行結果またはフィールドの型のデフォルトコンストラクタを使って生成したインスタンスがフィールドにセットされます。
	 * @return モデルオブジェクトを評価結果とするOGNL式
	 */
	public String exp() default "";
	
}
