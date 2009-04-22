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
package org.seasar.framework.container.creator;

import java.lang.reflect.Modifier;

import org.apache.wicket.Component;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.assembler.AutoProxyBindingAutoDef;
import org.seasar.framework.container.assembler.ProxyBindingTypeDef;
import org.seasar.framework.container.deployer.InstanceDefFactory;
import org.seasar.framework.container.factory.AnnotationHandler;
import org.seasar.framework.container.factory.AnnotationHandlerFactory;
import org.seasar.framework.convention.NamingConvention;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WicketコンポーネントをSMART DeployするためのCreator。
 * <p>
 * SAWicketを使用する際に、creator.diconファイルに、このクラスを記述することで、SMART Deployに沿った形で
 * WicketコンポーネントがS2Containerに登録され、Wicketコンポーネントをインスタンス化した時に自動的にDIされます。
 * </p>
 * <p>
 * S2Containerへの登録対象となるクラスの条件は以下の通りです。
 * </p>
 * <ul>
 * <li>Componentクラスを継承している</li>
 * <li><strike>メンバクラス、匿名クラスでない</strike>(現在この条件は無効です)</li>
 * <li>インターフェースでない</li>
 * <li>抽象クラスでない</li>
 * </ul>
 * 
 * @author TAKEUCHI Hideyuki (chimerast)
 */
public class WicketCreator extends ComponentCreatorImpl {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /** S2Containerへクラスを登録する際の接頭辞 */
    public static final String COMPONENTNAME_PREFIX = "sawicket$";

    public WicketCreator(NamingConvention namingConvention) {
        super(namingConvention);
        setNameSuffix(namingConvention.getPageSuffix());
        setInstanceDef(InstanceDefFactory.OUTER);
        setExternalBinding(true);
        setAutoBindingDef(AutoProxyBindingAutoDef.PROXY);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ComponentDef createComponentDef(Class componentClass) {
        if (!getNamingConvention().isTargetClassName(componentClass.getName())) {
            return null;
        }
        Class<?> targetClass = componentClass;
        if (!Component.class.isAssignableFrom(targetClass)) {
            return null;
        }
        /*- TODO: 必要かどうか判断 
        if (targetClass.isMemberClass() || targetClass.isLocalClass()
                || targetClass.isAnonymousClass()) {
            return null;
        }
        */
        if (targetClass.isInterface()) {
            return null;
        } else if (Modifier.isAbstract(targetClass.getModifiers())) {
            return null;
        }
        AnnotationHandler handler =
                AnnotationHandlerFactory.getAnnotationHandler();
        ComponentDef cd =
                handler.createComponentDef(targetClass, getInstanceDef(),
                        getAutoBindingDef(), isExternalBinding());
        cd.setComponentName(COMPONENTNAME_PREFIX
                + targetClass.getCanonicalName());
        handler.appendDI(cd);
        customize(cd);
        handler.appendInitMethod(cd);
        handler.appendDestroyMethod(cd);
        handler.appendAspect(cd);
        handler.appendInterType(cd);
        return cd;
    }

    @Override
    public void customize(ComponentDef componentDef) {
        super.customize(componentDef);

        int size = componentDef.getPropertyDefSize();
        for (int i = 0; i < size; ++i) {
            componentDef.getPropertyDef(i).setBindingTypeDef(
                    ProxyBindingTypeDef.PROXY);
        }
    }
}
