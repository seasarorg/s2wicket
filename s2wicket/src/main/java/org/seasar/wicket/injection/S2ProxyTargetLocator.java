/*-
 * Copyright 2011 TAKEUCHI Hideyuki (chimerast)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.wicket.injection;

import java.io.Serializable;

import org.apache.wicket.proxy.IProxyTargetLocator;
import org.seasar.framework.container.assembler.ProxyBindingTypeDef;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * S2Container用のProxyTargetLocator。
 * <p>
 * {@link ProxyBindingTypeDef}クラスにおいてコンポーネントの代わりにDIされたプロクシが実体を取り出す際に使用します。
 * </p>
 * 
 * @author TAKEUCHI Hideyuki (chimerast)
 */
public class S2ProxyTargetLocator implements IProxyTargetLocator, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(S2ProxyTargetLocator.class);

    private final Object key;

    /**
     * コンストラクタ。
     * 
     * @param key
     *            S2Containerからオブジェクトを取り出すためのキー
     */
    public S2ProxyTargetLocator(Object key) {
        this.key = key;
    }

    public Object locateProxyTarget() {
        return SingletonS2ContainerFactory.getContainer().getComponent(key);
    }
}
