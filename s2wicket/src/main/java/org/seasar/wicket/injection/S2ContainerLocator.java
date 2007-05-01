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

/**
 * Seasarコンテナオブジェクトを取得するための処理を持つクラスです。
 * @author Yoichiro Tanaka
 * @since 1.0.0
 */
class S2ContainerLocator implements Serializable, IS2ContainerLocator {
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 */
	S2ContainerLocator() {
		super();
	}
	
	/**
	 * WicketメタデータからSeasarコンテナオブジェクトを取り出します。
	 * @return Seasarコンテナオブジェクト
	 */
	public S2Container get() {
		Application application = Application.get();
		return ((S2ContainerHolder)application.getMetaData(S2ContainerHolder.META_DATA_KEY)).getContainer();
	}

}
