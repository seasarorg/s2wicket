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

import wicket.Component;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.TextField;
import wicket.markup.html.link.PageLink;
import wicket.markup.html.list.ListView;
import wicket.model.BoundCompoundPropertyModel;
import wicket.model.CompoundPropertyModel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.model.PropertyModel;

/**
 * モデルの種別を定義した列挙型です。<br />
 * <p>この列挙型は，{@link WicketModel}アノテーションによって指定されるモデルオブジェクトと，
 * モデルオブジェクトを使用するコンポーネントとの間で，どのように関連付けを行うかを指定するために使用します。</p>
 * <p>各フィールドについて，どのような関連付けとなるかを以下に示します。</p>
 * <ul>
 * <li>{@link #RAW} - モデルオブジェクトがそのままコンポーネントに関連付けられます。</li>
 * <li>{@link #BASIC} - モデルオブジェクトは{@link Model}オブジェクトとしてラップされてコンポーネントに関連付けられます。</li>
 * <li>{@link #PROPERTY} - モデルオブジェクトは{@link PropertyModel}オブジェクトとしてラップされてコンポーネントに関連付けられます。</li>
 * <li>{@link #COMPOUND_PROPERTY} - モデルオブジェクトは{@link CompoundPropertyModel}オブジェクトとしてラップされてコンポーネントに関連付けられます。</li>
 * <li>{@link #BOUND_COMPOUND_PROPERTY} - モデルオブジェクトは{@link BoundCompoundPropertyModel}オブジェクトとしてラップされてコンポーネントに関連付けられます。</li>
 * </ul>
 * <p>{@link #RAW}フィールドは，{@link PageLink#PageLink(String, Class)}や{@link ListView#ListView(String, java.util.List)}などのように，
 * {@link IModel}以外の引数を持つコンストラクタを使ってコンポーネントを生成したい場合に使用します。{@link #RAW}以外のフィールドは，
 * {@link Component#Component(String, IModel)}のように，{@link IModel}を引数に持つコンストラクタを使ってコンポーネントが生成されます。 </p>
 * <p>{@link ModelType}を{@link WicketModel#type()}属性で使用する例を以下に示します。</p>
 * <pre>
 * &#064;WicketModel(type=ModelType.RAW)
 * private Class nextPage;
 * &#064;WicketComponent(modelName="nextPage")
 * private PageLink nextPageLink;
 * </pre>
 * <p>上記では{@link #RAW}を指定することにより，nextPageLinkオブジェクトの生成時には，{@link PageLink#PageLink(String, Class)}コンストラクタが
 * 適用され，第２引数にはnextPageオブジェクトがそのまま渡されます。</p>
 * <pre>
 * &#064;WicketModel(type=ModelType.BASIC)
 * private String message;
 * &#064;WicketComponent(modelName="message")
 * private Label messageLabel;
 * </pre>
 * <p>上記では{@link #BASIC}を指定することにより，messageオブジェクトは{@link Model#Model(java.io.Serializable)}コンストラクタに渡されて
 * {@link Model}オブジェクトが生成され，{@link Label#Label(String, IModel)}コンストラクタの第２引数に渡されてmessageLabel
 * オブジェクトが生成されます。</p>
 * <pre>
 * &#064;WicketModel(type=ModelType.PROPERTY)
 * private SearchCondition condition;
 * &#064;WicketComponent(modelName="condition", modelProperty="keyword")
 * private TextField keywordField;
 * </pre>
 * <p>上記では{@link #PROPERTY}を指定することにより，conditionオブジェクトは{@link PropertyModel#PropertyModel(Object, String)}コンストラクタに
 * 渡されて{@link PropertyModel}オブジェクトが生成され，{@link TextField#TextField(String, IModel)}コンストラクタの第２引数に渡されて
 * keywordFieldオブジェクトが生成されます。これは，</p>
 * <pre>
 * PropertyModel pm = new PropertyModel(condition, "keyword");
 * keywordField = new TextField("keywordField", pm);
 * add(keywordField);
 * </pre>
 * <p>というコードと等価です。</p>
 * <pre>
 * &#064;WicketModel(type=ModelType.COMPOUND_PROPERTY)
 * private SearchCondition condition;
 * &#064;WicketComponent(modelName="condition")
 * private Form form;
 * </pre>
 * <p>上記では{@link #COMPOUND_PROPERTY}を指定することにより，conditionオブジェクトは
 * {@link CompoundPropertyModel#CompoundPropertyModel(Object)}コンストラクタに
 * 渡されて{@link CompoundPropertyModel}オブジェクトが生成され，{@link Form#Form(String, IModel)}コンストラクタの第２引数に渡されて
 * formオブジェクトが生成されます。これは，</p>
 * <pre>
 * CompoundPropertyModel cpm = new CompoundPropertyModel(condition);
 * form = new Form("form", cpm);
 * add(form);
 * </pre>
 * <p>というコードと等価です。</p>
 * <pre>
 * &#064;WicketModel(type=ModelType.BOUND_COMPOUND_PROPERTY)
 * private SearchCondition condition;
 * &#064;WicketComponent(modelName="condition")
 * private Form form;
 * </pre>
 * <p>上記では{@link #BOUND_COMPOUND_PROPERTY}を指定することにより，conditionオブジェクトは
 * {@link BoundCompoundPropertyModel#BoundCompoundPropertyModel(Object)}コンストラクタに
 * 渡されて{@link BoundCompoundPropertyModel}オブジェクトが生成され，{@link Form#Form(String, IModel)}コンストラクタの第２引数に渡されて
 * formオブジェクトが生成されます。これは，</p>
 * <pre>
 * BoundCompoundPropertyModel bcpm = new BoundCompoundPropertyModel(condition);
 * form = new Form("form", bcpm);
 * add(form);
 * </pre>
 * <p>というコードと等価です。{@link #BOUND_COMPOUND_PROPERTY}フィールドを使用した場合，そのモデルが関連付けられたコンポーネントを親に持つ
 * コンポーネントが定義された時に，そのコンポーネントとモデルが自動的に{@link BoundCompoundPropertyModel#bind(Component, String)}
 * メソッドを実行してバインドされます。バインドされるプロパティ名として，{@link WicketComponent#modelProperty()}属性が指定されていた場合はその指定が採用され，
 * {@link WicketComponent#modelProperty()}属性の指定がなかったときは，対象コンポーネントのフィールド名が採用されます。例えば，</p>
 * <pre>
 * &#064;WicketComponent(parent="form", modelProperty="keyword")
 * private TextField keywordField;
 * </pre>
 * <p>という定義は，</p>
 * <pre>
 * keywordField = new TextField("keywordField");
 * bcpm.bind(keywordField, "keyword");
 * form.add(keywordField);
 * </pre>
 * <p>というコードと等価です。</p>
 * 
 * @see WicketModel
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
public enum ModelType {

	/**
	 * このモデルオブジェクトをそのままコンポーネントに関連付けます。
	 */
	RAW,
	
	/**
	 * 基本モデルとして，このモデルオブジェクトをコンポーネントに関連付けします。
	 */
	BASIC,
	
	/**
	 * プロパティモデルとして，このモデルオブジェクトをコンポーネントに関連付けします。
	 */
	PROPERTY,
	
	/**
	 * 複合プロパティモデルとして，このモデルオブジェクトをコンポーネントに関連付けします。
	 */
	COMPOUND_PROPERTY,
	
	/**
	 * 複合バインドプロパティモデルとして，このモデルオブジェクトをコンポーネントに関連付けします。
	 */
	BOUND_COMPOUND_PROPERTY
	
}
