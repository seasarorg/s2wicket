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

import org.seasar.framework.container.S2Container;

import wicket.Application;
import wicket.MetaDataKey;

/**
 * Seasarコンテナオブジェクトを保持するクラスです。
 * @author Yoichiro Tanaka
 * @since 1.0.0
 */
class S2ContainerHolder implements Serializable {
	
	/** このホルダーをWicketメタデータとして格納する際のキー */
	static final MetaDataKey META_DATA_KEY = new MetaDataKey(S2ContainerHolder.class) {};
	
	/** Seasarコンテナオブジェクト */
	private S2Container container;
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 * @param container Seasarコンテナオブジェクト
	 */
	private S2ContainerHolder(S2Container container) {
		super();
		// 引数チェック
		if (container == null)
			throw new IllegalArgumentException("container is null.");
		// 引数をフィールドに保持
		this.container = container;
	}
	
	/**
	 * Seasarコンテナオブジェクトを返します。
	 * @return Seasarコンテナオブジェクト
	 */
	S2Container getContainer() {
		return container;
	}
	
	/**
	 * SeasarコンテナオブジェクトをWicketメタデータとして格納します。
	 * @param application アプリケーションオブジェクト
	 * @param container Seasarコンテナオブジェクト
	 */
	static void store(Application application, S2Container container) {
		S2ContainerHolder holder = new S2ContainerHolder(container);
		application.setMetaData(META_DATA_KEY, holder);
	}
	
}
