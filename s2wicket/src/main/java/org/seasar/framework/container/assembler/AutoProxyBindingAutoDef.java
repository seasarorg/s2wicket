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

import org.seasar.framework.container.AutoBindingDef;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.PropertyAssembler;

/**
 * プロクシ自動バインディング定義の自動版。
 * 
 * @author TAKEUCHI Hideyuki (chimerast)
 */
public class AutoProxyBindingAutoDef extends AutoBindingAutoDef {
    public static final AutoBindingDef PROXY = new AutoProxyBindingAutoDef(
            "proxy");

    public AutoProxyBindingAutoDef(String name) {
        super(name);
    }

    @Override
    public PropertyAssembler createPropertyAssembler(ComponentDef componentDef) {
        return new AutoProxyPropertyAssembler(componentDef);
    }
}
