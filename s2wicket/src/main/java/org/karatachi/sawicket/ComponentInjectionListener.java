/*-
 * Copyright 2008 TAKEUCHI Hideyuki (chimerast)
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
package org.karatachi.sawicket;

import org.apache.wicket.Component;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.creator.WicketCreator;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.container.util.SmartDeployUtil;

/**
 * WicketのComponent初期化時に依存性注入を行うためのコンポーネント初期化リスナ。
 * SAWicketFilter内でWebApplication初期化後に自動的に登録します。
 * 
 * @author TAKEUCHI Hideyuki (chimerast)
 */
class ComponentInjectionListener implements IComponentInstantiationListener {
    public void onInstantiation(Component component) {
        Class<?> target = component.getClass();
        if (target.getCanonicalName() == null) {
            return;
        }
        String name =
                WicketCreator.COMPONENTNAME_PREFIX + target.getCanonicalName();

        S2Container container = SingletonS2ContainerFactory.getContainer();

        // WarmDeploy時のComponentのロード＆登録
        if (SmartDeployUtil.isWarmdeployMode(container)) {
            try {
                container.getComponentDef(target);
            } catch (ComponentNotFoundRuntimeException ignore) {
            }
        }

        // コンポーネントが名前で登録されていれば注入
        if (container.hasComponentDef(name)) {
            container.injectDependency(component, name);
        }
    }
}
