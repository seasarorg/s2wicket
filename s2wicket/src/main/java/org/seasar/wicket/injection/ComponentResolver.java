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

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.seasar.framework.container.S2Container;

/**
 * SeasarコンポーネントをSeasarコンテナからルックアップする処理を持つコンポーネントリゾルバクラスです。
 * @author Yoichiro Tanaka
 * @since 1.0.0
 */
class ComponentResolver implements Serializable {
	
	/** コンポーネント名 */
	private String componentName;
	/** コンポーネントの型の名前 */
	private String componentTypeName;
	/** コンポーネントの型のキャッシュ */
	private transient Class componentTypeCache;
	/** Seasarコンテナロケータ */
	private IS2ContainerLocator containerLocator;
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 * @param componentName コンポーネント名 
	 * @param componentType コンポーネントの型
	 * @param containerLocator Seasarコンテナロケータ
	 */
	ComponentResolver(String componentName, Class componentType, IS2ContainerLocator containerLocator) {
		super();
		// 引数チェック
		if (componentType == null)
			throw new IllegalArgumentException("componentType is null.");
		if (containerLocator == null)
			throw new IllegalArgumentException("containerLocator is null.");
		// 引数をフィールドに保持
		this.componentName = componentName;
		this.componentTypeName = componentType.getName();
		this.containerLocator = containerLocator;
	}
	
	/**
	 * コンポーネントの型オブジェクトを返します。
	 * コンポーネントの型はキャッシュされています。もしセッションがPassivateされた際などには，キャッシュは消去されます。
	 * その際には，再度型オブジェクトがクラスローダにより読み込まれます。
	 * @return コンポーネントの型オブジェクト
	 */
	private Class getComponentType() {
		if (componentTypeCache == null) {
			try {
				componentTypeCache = Class.forName(componentTypeName, true, Thread.currentThread().getContextClassLoader());
				return componentTypeCache;
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("Class[" + componentTypeName + "] loading failed.", e);
			}
		} else {
			return componentTypeCache;
		}
	}
	
	/**
	 * インジェクション対象のオブジェクトをSeasarコンテナよりルックアップし，その結果を返します。
	 * @return インジェクション対象のオブジェクト
	 */
	Object getTargetObject() {
		// コンテナを取得
		S2Container container = containerLocator.get();
		// コンポーネント名が指定されたかチェック
		if (StringUtils.isEmpty(componentName)) {
			// 型を使ってコンポーネントオブジェクトをルックアップ
			return lookupSeasarComponentByType(container);
		} else {
			// 名前を使ってコンポーネントオブジェクトをルックアップ
			return lookupSeasarComponentByName(container);
		}
	}

	/**
	 * コンポーネント名を使って，Seasarコンテナに登録されたコンポーネントオブジェクトをルックアップし，その結果を返します。
	 * @param container コンテナオブジェクト
	 * @return コンポーネントオブジェクト
	 */
	private Object lookupSeasarComponentByName(S2Container container) {
		// コンテナよりルックアップ
		Object component = container.getComponent(componentName);
		// 結果を返却
		return component;
	}

	/**
	 * コンポーネントの型を使って，Seasarコンテナに登録されたコンポーネントオブジェクトをルックアップし，その結果を返します。
	 * @param container コンテナオブジェクト
	 * @return コンポーネントオブジェクト
	 */
	private Object lookupSeasarComponentByType(S2Container container) {
		// コンポーネントの型を取得
		Class componentType = getComponentType();
		// コンテナよりルックアップ
		Object component = container.getComponent(componentType);
		// 結果を返却
		return component;
	}
	
	/**
	 * このオブジェクトと引数で与えられたオブジェクトの内容の一致性を返します。
	 * @param obj 比較対象のオブジェクト
	 * @return 内容が一致すれば true
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ComponentResolver)) {
			return false;
		} else {
			ComponentResolver target = (ComponentResolver)obj;
			return new EqualsBuilder()
				.append(componentName, target.componentName)
				.append(componentTypeName, target.componentTypeName)
				.isEquals();
		}
	}
	
	/**
	 * ハッシュ値を返します。
	 * @return ハッシュ値
	 */
	public int hashCode() {
		int result = componentTypeName.hashCode();
		if (componentName != null) {
			result += componentName.hashCode() * 127;
		}
		return result;
	}

}
