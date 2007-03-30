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
import wicket.MarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.form.Button;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.SubmitLink;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.model.BoundCompoundPropertyModel;
import wicket.model.IModel;
import wicket.model.PropertyModel;

/**
 * Wicketコンポーネントの生成をS2Wicketに指示するためのアノテーションです。<br />
 * <p>Wicketを使用した開発では，ページを構成する複数のコンポーネントを生成し，それを親コンポーネントに登録するという，
 * コンポーネントツリーの構築処理を記述する必要があります。これは，SwingやSWTに代表されるGUIアプリケーションにおける
 * 画面の構築処理の記述と非常に近いものです。コンポーネントツリーの構築処理の記述は，インスタンス生成と親コンポーネントへの
 * 登録処理の繰り返しであり，必要な記述量が多い割には内容は冗長であり，可読性を高めることが困難です。</p>
 * <p>{@link WicketComponent}アノテーションを使用することで，以下の処理をS2Wicketに行わせることができます。</p>
 * <ul>
 * <li>コンポーネントのインスタンス生成。</li>
 * <li>モデルとの関連付け。</li>
 * <li>親コンポーネントへの登録。</li>
 * <li>イベント処理の呼び出し。</li>
 * </ul>
 * <p>{@link WicketComponent}アノテーションが付与されたフィールドは，まず「"create"+[フィールド名]+"Component"」という
 * 命名規則のメソッドが，フィールドが定義されたクラスに存在するかどうかチェックを行います。もし存在した場合には，
 * そのメソッドの戻り値がコンポーネントのインスタンスとしてフィールドにセットされます。例えば，</p>
 * <pre>
 * &#064;WicketComponent
 * private TextField firstName;
 * </pre>
 * <p>というフィールド定義があったとすると，</p>
 * <pre>
 * public TextField createFirstNameComponent() {
 *     return new TextField("firstName");
 * }
 * </pre>
 * <p>というようにcreateFirstNameComponent()メソッドを定義しておくことによって，S2Wicketにより自動的に呼び出されて，
 * 結果がfirstNameフィールドにセットされます。</p>
 * <p>もしcreate～Component()メソッドが定義されていなかった場合，S2Wicketによってコンポーネントのインスタンスが自動的に生成され，
 * 暗黙的にフィールドにセットされます。このときに使用されるコンストラクタは，モデルと関連付けるかどうかで使用するコンストラクタが異なります。
 * モデルとの関連付けを行わない場合は，wicket:idのみを受け取るコンストラクタが適用されます。
 * 関連付けるモデルの{@link WicketModel#type()}属性に{@link ModelType#RAW}が指定されていた場合は，wicket:id
 * および{@link WicketModel}アノテーションが付与されたモデルフィールドのオブジェクトを受け取ることができる型の引数を持つ
 * コンストラクタが適用されます。また，{@link ModelType#RAW}以外が指定されたモデルフィールドのオブジェクトと関連付けを行う場合は，
 * wicket:idおよび{@link IModel}オブジェクトを受け取るコンストラクタが適用されます。</p>
 * <p>コンポーネントのインスタンスを生成する際のwicket:id文字列は，{@link #wicketId()}属性で指定された文字列が適用されます。
 * もし{@link #wicketId()}属性を省略した場合は，フィールド名がwicket:idとして適用されます。例えば，</p>
 * <pre>
 * &#064;WicketComponent(wicketId="firstName")
 * private TextField firstNameField;
 * &#064;WicketComponent
 * private TextField lastNameField;
 * </pre>
 * <p>という記述の場合，firstNameFieldコンポーネントのwicket:idは"firstName"が，
 * lastNameFieldコンポーネントのwicket:idは"lastNameField"が採用されます。</p>
 * <p>生成したコンポーネントは，それらを管理するコンテナコンポーネント（{@link WebPage}，{@link Panel}，{@link Form}など）
 * に登録する必要があります。{@link #parent()}属性に親のコンテナコンポーネントとするコンポーネントのフィールド名を指定することで，
 * 親コンポーネントの{@link MarkupContainer#add(Component)}メソッドにコンポーネントが渡され，登録されます。
 * つまり，{@link #parent()}属性によって，コンポーネントツリーが決定されます。{@link #parent()}属性の指定が省略された場合，
 * そのフィールドが定義されたクラスのインスタンス自身が登録対象となります。例えば，</p>
 * <pre>
 * public class InputPage extends WebPage {
 *     &#064;@WicketComponent
 *     private Form form;
 *     &#064;@WicketComponent(parent="form")
 *     private TextField firstNameField;
 * }
 * </pre>
 * <p>という記述の場合，firstNameFieldコンポーネントはformコンテナコンポーネントに登録され，
 * formコンテナコンポーネントはInputPageページコンポーネントに登録されます。</p>
 * <p>コンポーネントは，通常何らかのモデルオブジェクトあるいはモデルオブジェクトのプロパティを利用します。
 * {@link #modelName()}属性および{@link #modelProperty()}属性を指定することで，
 * コンポーネントに関連付けるモデルを明示的に指定することができます。{@link #modelName()}属性に
 * モデルとして関連付けを行いたい{@link WicketModel}アノテーションが付与されたモデルフィールドの
 * フィールド名を指定します。これによって，コンポーネントのインスタンス生成時に，指定されたフィールド名
 * に基づいてモデルがコンポーネントのコンストラクタに渡されて関連付けられます。</p>
 * <p>もし{@link #modelName()}属性を省略したときは，以下の順序で適用するモデルを暗黙的に決定します。</p>
 * <ol>
 * <li>{@link #parent()}属性により親コンポーネントが指定されていて，さらに親コンポーネントに関連付けられたモデルフィールドの
 * {@link WicketModel#type()}属性に{@link ModelType#RAW}，{@link ModelType#BASIC}，{@link ModelType#PROPERTY}
 * が指定されていたときは，矛盾した指定と判断し，{@link UnsupportedOperationException}例外がスローされる。</li>
 * <li>{@link #parent()}属性により親コンポーネントが指定されていて，さらに親コンポーネントに関連付けられたモデルフィールドの
 * {@link WicketModel#type()}属性に{@link ModelType#COMPOUND_PROPERTY}が指定されていたときは，
 * コンポーネントに対してモデルの関連付けを行わない。</li>
 * <li>{@link #parent()}属性により親コンポーネントが指定されていて，さらに親コンポーネントに関連付けられたモデルフィールドの
 * {@link WicketModel#type()}属性に{@link ModelType#BOUND_COMPOUND_PROPERTY}が指定されていたときは，
 * コンポーネントに対してモデルの関連付けは行わないが，コンポーネントは{@link #modelProperty()}属性で指定された
 * プロパティ名で，{@link BoundCompoundPropertyModel#bind(Component, String)}の呼び出しにより，
 * 親コンポーネントに関連付けられた{@link BoundCompoundPropertyModel}オブジェクトとバインドされる。</li>
 * <li>{@link #parent()}属性により親コンポーネントが指定されているが明示的にモデル名が指定されていない場合，
 * あるいは{@link #parent()}属性が省略されて親コンポーネントが指定されていない場合で，
 * さらに{@link WicketModel}アノテーションによるモデルフィールドが１つだけ存在しているときは，
 * 以下の動作を行う。
 * <ul>
 * <li>モデルフィールドの{@link WicketModel#type()}属性に{@link ModelType#RAW}または{@link ModelType#BASIC}
 * が指定されていたときは，コンポーネントに対してモデルの関連付けを行わない。</li>
 * <li>モデルフィールドの{@link WicketModel#type()}属性に{@link ModelType#PROPERTY}が指定されていたときは，
 * {@link #modelProperty()}属性で指定されたプロパティ名でコンポーネントと関連付けを行う。
 * {@link #modelProperty()}属性が省略された場合は，コンポーネントのフィールド名がプロパティ名として適用される。</li>
 * <li>モデルフィールドの{@link WicketModel#type()}属性に{@link ModelType#COMPOUND_PROPERTY}または
 * {@link ModelType#BOUND_COMPOUND_PROPERTY}が指定されたときは，矛盾した指定と判断し，{@link UnsupportedOperationException}
 * 例外がスローされる。</li>
 * </ul>
 * </li>
 * </ol>
 * <p>{@link WicketComponent}アノテーションが付与されたコンポーネントは，それぞれ発生したイベントの種別に従って，
 * 呼び出されるメソッドが定義されています。また，{@link ListView}クラスのpopulate()メソッドのように，
 * 表示する情報を与えるためにコールバックされるメソッドも存在します。{@link WicketComponent}アノテーションが
 * 付与されたコンポーネントについて，「[メソッド名]+[フィールド名]」という命名規則に従って決定されるメソッドを定義しておくことにより，
 * コンポーネントのメソッドがWicketによって呼び出された際に，定義したメソッドがS2Wicketによって実行されます。
 * これは，対象のメソッドが以下のどちらかの条件を満たしている場合に限ります。</p>
 * <ul>
 * <li>抽象メソッドであること。</li>
 * <li>スコープがprotectedであり，戻り値の型がvoidであること。</li>
 * </ul>
 * <p>定義するメソッドの引数や戻り値の型は，オリジナルの抽象メソッドと一致している必要があります。
 * 
 * @see WicketAction
 * @see WicketModel
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface WicketComponent {
	
	/**
	 * 生成するコンポーネントのwicket:idを指定するための属性（任意）です。
	 * この属性を省略した場合，このアノテーションが付与されたフィールドの名前がwicket:idとして採用されます。
	 * @return wicket:idとする文字列
	 */
	public String wicketId() default "";
	
	/**
	 * 生成するコンポーネントを登録する親のコンテナコンポーネントのフィールド名を指定するための属性（任意）です。
	 * この属性を省略した場合，または"this"を指定した場合は，
	 * このアノテーションが付与されたフィールドが所属するコンポーネントが親コンテナコンポーネントとして採用されます。
	 * @return 親のコンテナコンポーネントのフィールド名
	 */
	public String parent() default "";
	
	/**
	 * 生成するコンポーネントと関連付けを行うモデルオブジェクトのフィールド名を指定するための属性（任意）です。
	 * 
	 * @return
	 */
	public String modelName() default "";
	
	public String modelProperty() default "";
	
	public WicketAction[] actions() default {};
	
}
