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

package org.seasar.wicket.injection.fieldfilters;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.seasar.wicket.injection.FieldFilter;

/**
 * 与えられたフィールド名のパターンに従ってインジェクション対象のフィールドかどうかを判断するフィールドフィルタクラスです。<br />
 * <p>このフィールドフィルタを使用することにより，フィールド名の命名規則に従ってインジェクションが行われるようになります。
 * </p>
 * @author Yoichiro Tanaka
 * @since 1.2.0
 */
public class FieldNamePatternFieldFilter implements FieldFilter {
	
//	--- パターンフィールド
	
	/** パッケージを特定するための正規表現パターン文字列 */
	private String packageNamePatternRegex;
	
	/** クラスを特定するための正規表現パターン文字列 */
	private String classNamePatternRegex;
	
	/** フィールドを特定するための正規表現パターン文字列 */
	private String fieldNamePatternRegex;
	
	/** パッケージを特定するためのコンパイル済み正規表現パターン */
	private Pattern packageNamePattern;
	
	/** クラスを特定するためのコンパイル済み正規表現パターン */
	private Pattern classNamePattern;
	
	/** フィールドを特定するためのコンパイル済み正規表現パターン */
	private Pattern fieldNamePattern;
	
//	--- コンストラクタ

	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 */
	public FieldNamePatternFieldFilter() {
		super();
	}
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 * @param packageNamePatternRegex パッケージを特定するための正規表現パターン文字列
	 * @param classNamePatternRegex クラスを特定するための正規表現パターン文字列
	 * @param fieldNamePatternRegex フィールドを特定するための正規表現パターン文字列
	 */
	public FieldNamePatternFieldFilter(
			String packageNamePatternRegex, String classNamePatternRegex, String fieldNamePatternRegex) {
		super();
		setPackageNamePatternRegex(packageNamePatternRegex);
		setClassNamePatternRegex(classNamePatternRegex);
		setFieldNamePatternRegex(fieldNamePatternRegex);
	}
	
//	--- プロパティメソッド
	
	/**
	 * クラスを特定するための正規表現パターン文字列を返します。
	 * @return クラスを特定するための正規表現パターン文字列
	 */
	public String getClassNamePatternRegex() {
		return classNamePatternRegex;
	}

	/**
	 * クラスを特定するための正規表現パターン文字列をセットします。
	 * @param classNamePatternRegex クラスを特定するための正規表現パターン文字列
	 */
	public void setClassNamePatternRegex(String classNamePatternRegex) {
		if (classNamePatternRegex == null)
			throw new IllegalArgumentException("classNamePatternRegex is null.");
		if (classNamePatternRegex.length() == 0)
			throw new IllegalArgumentException("classNamePatternRegex is empty.");
		classNamePattern = Pattern.compile(classNamePatternRegex);
		this.classNamePatternRegex = classNamePatternRegex;
	}

	/**
	 * フィールドを特定するための正規表現パターン文字列を返します。
	 * @return フィールドを特定するための正規表現パターン文字列
	 */
	public String getFieldNamePatternRegex() {
		return fieldNamePatternRegex;
	}

	/**
	 * フィールドを特定するための正規表現パターン文字列をセットします。
	 * @param fieldNamePatternRegex フィールドを特定するための正規表現パターン文字列
	 */
	public void setFieldNamePatternRegex(String fieldNamePatternRegex) {
		if (fieldNamePatternRegex == null)
			throw new IllegalArgumentException("fieldNamePatternRegex is null.");
		if (fieldNamePatternRegex.length() == 0)
			throw new IllegalArgumentException("fieldNamePatternRegex is empty.");
		fieldNamePattern = Pattern.compile(fieldNamePatternRegex);
		this.fieldNamePatternRegex = fieldNamePatternRegex;
	}

	/**
	 * パッケージを特定するための正規表現パターン文字列を返します。
	 * @return パッケージを特定するための正規表現パターン文字列
	 */
	public String getPackageNamePatternRegex() {
		return packageNamePatternRegex;
	}

	/**
	 * パッケージを特定するための正規表現パターン文字列をセットします。
	 * @param packageNamePatternRegex パッケージを特定するための正規表現パターン文字列
	 */
	public void setPackageNamePatternRegex(String packageNamePatternRegex) {
		if (packageNamePatternRegex == null)
			throw new IllegalArgumentException("packageNamePatternRegex is null.");
		if (packageNamePatternRegex.length() == 0)
			throw new IllegalArgumentException("packageNamePatternRegex is empty.");
		packageNamePattern = Pattern.compile(packageNamePatternRegex);
		this.packageNamePatternRegex = packageNamePatternRegex;
	}	

//	--- FieldFilter実装メソッド
	
	/**
	 * 指定されたインジェクション対象となるフィールドに対してインジェクションするコンポーネントオブジェクトのコンポーネント名を返します。
	 * この実装クラスでは，フィールド名をルックアップするコンポーネントオブジェクトのコンポーネント名として返します。
	 * @param field インジェクション対象となるフィールド
	 * @return ルックアップするコンポーネントオブジェクトのコンポーネント名
	 * @see org.seasar.wicket.injection.FieldFilter#getLookupComponentName(java.lang.reflect.Field)
	 */
	public String getLookupComponentName(Field field) {
		if (field == null)
			throw new IllegalArgumentException("field is null.");
		return field.getName();
	}

	/**
	 * 指定されたフィールドがインジェクション対象かどうかを返します。
	 * この実装クラスでは，{@link #setPackageNamePatternRegex(String)}，{@link #setClassNamePatternRegex(String)}，
	 * {@link #setFieldNamePatternRegex(String)}のそれぞれのメソッドで指定された正規表現パターンに，
	 * 指定されたフィールドが合致するかどうかをチェックし，その結果を返します。
	 * @param field チェック対象のフィールド
	 * @return インジェクション対象と判断された場合は true
	 * @see org.seasar.wicket.injection.FieldFilter#isSupported(java.lang.reflect.Field)
	 */
	public boolean isSupported(Field field) {
		if (field == null)
			throw new IllegalArgumentException("field is null.");
		if (packageNamePattern == null)
			throw new IllegalStateException("packageNamePatternRegex not set.");
		if (classNamePattern == null)
			throw new IllegalStateException("classNamePatternRegex not set.");
		if (fieldNamePattern == null)
			throw new IllegalStateException("fieldNamePatternRegex not set.");
		Class<?> clazz = field.getDeclaringClass();
		Package pkg = clazz.getPackage();
		Matcher matcher = packageNamePattern.matcher(pkg.getName());
		if (matcher.matches()) {
			matcher = classNamePattern.matcher(clazz.getSimpleName());
			if (matcher.matches()) {
				matcher = fieldNamePattern.matcher(field.getName());
				return matcher.matches();
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
