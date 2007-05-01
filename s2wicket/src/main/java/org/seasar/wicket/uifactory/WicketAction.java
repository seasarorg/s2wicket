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
import wicket.markup.html.link.Link;

/**
 * コンポーネントのイベントハンドラの呼び出し時に実行したい処理を定義するためのアノテーションです。<br />
 * <p>Wicketが提供する各コンポーネントでは，Webブラウザ上で行われるイベント（フォームのサブミットやリンクのクリックなど）に応じて，
 * 呼び出されるメソッドがそれぞれ定義されています。それらのイベントハンドラメソッドは，抽象メソッドとして定義されているので，
 * 例えば{@link Link#onClick()}メソッドを（匿名クラスなどを作って）実装することで，アプリケーション独自のイベント処理を
 * 定義します。</p>
 * <p>S2Wicketでは，コンポーネントに対するイベントについて，コンポーネントをサブクラス化することなく，
 * 命名規則によって決定されるイベントハンドラメソッドが呼び出されます。ただし，多くのアプリケーションでは，プレゼンテーション層で
 * 行われる処理は（ビジネスロジックを呼び出すのみ，など）単純なことが多く，そのためだけにメソッドを定義することは
 * 無駄に感じる場面も少なくありません。</p>
 * <p>{@link WicketAction}アノテーションを使用することで，コンポーネントに発生したイベントについて，
 * サブクラス化などを行うことなく，より簡単な記述でイベント処理を定義することができます。{@link WicketAction}アノテーションでは，
 * 以下のイベント処理機構が提供されます。</p>
 * <ul>
 * <li>OGNL式によるイベント処理の定義。</li>
 * <li>{@link Component#setResponsePage(Class)}によるページ遷移。</li>
 * </ul>
 * <p>どのイベントハンドラメソッドに適用するかを，{@link #method()}属性で指定します。この属性には，
 * 適用するイベントハンドラのメソッド名をそのまま記述します。例えば，{@link Link#onClick()}メソッドに適用したい場合は，
 * 「method="onClick"」と記述します。</p>
 * <p>イベント発生時に実行したいOGNL式は，{@link #exp()}属性に記述します。記述したOGNL式は，イベントの発生時に
 * 評価されます。イベントハンドラメソッド自体は処理結果を返却しないvoidなので，OGNL式の評価結果について何らかのフィールド
 * に代入されるということはありません。例えば，何らかのビジネスロジックの呼び出し結果をモデルフィールドに代入する，
 * ということをしたい場合は，モデルフィールドへの代入式もOGNL式に含める必要があります。</p>
 * <p>{@link #exp()}属性で指定されたOGNL式は，S2Wicketが提供するコンテキスト内で評価されます。
 * コンテキスト内のルートオブジェクトは，処理対象のコンポーネントが所属しているオブジェクトです。例えば，</p>
 * <pre>
 * public class InputPage extends WebPage {
 *     &#064;WicketComponent(actions={
 *             &#064;WicketAction(method="onClick", exp="logic1()")
 *         }
 *     }
 *     private Link link;
 *     public void logic1() {
 *         // ビジネスロジック
 *     }
 * }
 * </pre>
 * <p>という記述の場合は，linkコンポーネントが所属するInputPageクラスに定義されたlogic1()メソッドが実行されます。</p>
 * <p>イベント発生の結果，別のページに遷移したいときは，{@link #responsePage()}属性を使用して，次ページを
 * 指定することができます。{@link #responsePage()}属性には，遷移したいページのクラス名を記述します。
 * 記述されたクラス名からその{@link Class}オブジェクトを取得し，それを{@link Component#setResponsePage(Class)}メソッドに
 * 渡してページ遷移を行います。この際，記述されたクラス名のクラスオブジェクトを，以下の順で検索します。</p>
 * <ol>
 * <li>{@link Class#forName(String)}メソッドに，記述されたクラス名（FQCNで記述されたことを想定）をそのまま渡して，クラスオブジェクトを得る。</li>
 * <li>コンポーネントが所属するページクラスのパッケージ名を記述されたクラス名の先頭に追加し，
 * それを{@link Class#forName(String)}に渡してクラスオブジェクトを得る。</li>
 * </ol>
 * <p>例えば，</p>
 * <pre>
 * package pkg1;
 * public class InputPage extends WebPage {
 *     &#064;WicketComponent(actions={
 *             &#064;WicketAction(method="onClick", responsePage="ConfirmPage")
 *         }
 *     }
 *     private Link link;
 * }
 * </pre>
 * <p>という記述の場合は，pkg1.ConfirmPageクラスが遷移するページのクラスとして適用されます。</p>
 * <p>{@link WicketAction}アノテーションにおいて，{@link #method()}属性は省略することはできません。
 * それに対して，{@link #exp()}属性および{@link #responsePage()}属性はどちらも省略することができます。
 * 両方指定した場合は，{@linkplain #exp()}属性で指定したOGNL式の評価後に，{@link #responsePage()}
 * 属性で指定されたページ遷移の処理が行われます。</p>
 * <p>OGNL式の評価中に何らかの例外が発生した場合，S2Wicketは対象のコンポーネントが所属するクラスのhandleException()
 * メソッドを呼び出します。handleException()メソッドでは，以下の引数を定義します。</p>
 * <ul>
 * <li>第1引数 - {@link Component}（イベント処理対象のコンポーネントオブジェクト）</li>
 * <li>第2引数 - {@link String}（イベントハンドラメソッド名）</li>
 * <li>第3引数 - {@link Exception}（発生した例外オブジェクト）</li>
 * </ul>
 * <p>開発者はhandleException()メソッドの中で，引数の情報を元に独自の例外処理を記述します。
 * handleException()メソッドが呼び出された場合，{@link #responsePage()}属性で指定されたページ遷移は
 * 行われません。例外発生時に別ページに遷移したい場合は，handleException()メソッド内で{@link Component#setResponsePage(Class)}
 * メソッドの呼び出しなどの記述を行ってください。</p>
 * <p>{@link WicketAction}アノテーションの使用例を以下に示します。</p>
 * <pre>
 * package emp;
 * public class CreateEmployeeConfirmPage extends WebPage {
 *     &#064;WicketModel
 *     private EmployeeModel employeeModel;
 *     ...
 *     &#064;WicketComponent(actions={
 *             &#064;WicketAction(
 *                 method="onSubmit",
 *                 exp="logic.create(employeeModel)"
 *                 responsePage="CreateEmployeeCompletePage")
 *         }
 *     )
 *     private Form form;
 *     &#064;SeasarComponent
 *     private EmployeeLogic logic;
 *     public void handleException(Component target, String methodName, Exception e) {
 *         if ((target == form) && (e instanceof DivisionNotFoundException)) {
 *             setResponsePage(SelectDivisionPage.class);
 *         } else {
 *             throw IllegalStateException("Unsupported exception", exception);
 *         }
 *     }
 * }
 * </pre>
 * <p>上記では，フォームのサブミット時に，OGNL式で指定された処理
 * （logicオブジェクトのcreate()メソッドにemployeeModelオブジェクトを渡して呼び出す）
 * を実行し，正常終了すればemp.CreateEmployeeCompletePageクラスのページに遷移します。
 * もしlogicオブジェクトのcreate()メソッド呼び出し時にDivisionNotFoundException例外が発生した場合は，
 * emp.SelectDivisionPageクラスのページに遷移し，それ以外の場合は不具合として{@link IllegalStateException}
 * 例外をスローしています。</p>
 * <p>{@link WicketAction}アノテーションが適用されるメソッドは，</p>
 * <ul>
 * <li>抽象メソッドである。</li>
 * <li>スコープがprotectedであり，戻り値の型がvoidである。</li>
 * </ul>
 * <p>のどちらかの条件を満たしているメソッドに限ります。</p>
 * 
 * @see WicketComponent
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
public @interface WicketAction {
	
	/** 処理対象とするイベントハンドラメソッドのメソッド名 */
	public String method();
	
	/** イベント発生時に実行（評価）したいOGNL式 */
	public String exp() default "";
	
	/** 次に遷移したいページのクラス名 */
	public String responsePage() default "";
	
}
