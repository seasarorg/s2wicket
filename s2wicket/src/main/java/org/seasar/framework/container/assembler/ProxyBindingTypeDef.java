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
package org.seasar.framework.container.assembler;

import org.apache.wicket.proxy.LazyInitProxyFactory;
import org.seasar.framework.beans.IllegalPropertyRuntimeException;
import org.seasar.framework.container.BindingTypeDef;
import org.seasar.framework.container.ComponentDef;
import org.seasar.wicket.injection.S2ProxyTargetLocator;

/**
 * 実インスタンスの代わりにProxyオブジェクトを生成してコンポーネントに注入するバインディングタイプ定義。
 * <p>
 * WicketコンポーネントのフィールドはすべてSerializableを実装している必要があるため、
 * S2Containerに登録されたコンポーネントの代わりにwicket-iocのLasyInitProxyFactoryを
 * 使用して、コンポーネントのプロクシをDIします。
 * </p>
 * 
 * @author TAKEUCHI Hideyuki (chimerast)
 */
public class ProxyBindingTypeDef extends BindingTypeMayDef {
    public static final BindingTypeDef PROXY = new ProxyBindingTypeDef("proxy");

    protected ProxyBindingTypeDef(String name) {
        super(name);
    }

    @Override
    protected Object getValue(ComponentDef componentDef, Object key,
            Object component, String propertyName)
            throws IllegalPropertyRuntimeException {
        if (key instanceof Class<?>) {
            // keyがclassまたはinterfaceの場合は、プロパティのクラスタイプなので
            // そのクラスタイプでproxyの作成
            return LazyInitProxyFactory.createProxy((Class<?>) key,
                    new S2ProxyTargetLocator(key));
        } else {
            // keyがコンポーネント名の場合があるのかわからないが、
            // 一度値を取り出してそのクラスタイプでpxoryを作成
            Object value =
                    super.getValue(componentDef, key, component, propertyName);
            return LazyInitProxyFactory.createProxy(value.getClass(),
                    new S2ProxyTargetLocator(key));
        }
    }
}
