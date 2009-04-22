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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.wicket.Application;
import org.apache.wicket.application.ReloadingClassLoader;
import org.apache.wicket.protocol.http.ReloadingWicketFilter;
import org.apache.wicket.protocol.http.WebApplication;
import org.seasar.framework.container.ExternalContext;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.deployer.ComponentDeployerFactory;
import org.seasar.framework.container.deployer.ExternalComponentDeployerProvider;
import org.seasar.framework.container.external.servlet.HttpServletExternalContext;
import org.seasar.framework.container.external.servlet.HttpServletExternalContextComponentDefRegister;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.container.util.SmartDeployUtil;
import org.seasar.framework.exception.EmptyRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * S2Containerに対応させたWicketFilter。
 * <p>
 * web.xmlにはこのフィルタを登録すれば、WicketおよびS2Container双方とも使用できるようになります。
 * WicketFilterおよびS2ContainerFilterは登録しないでください。
 * </p>
 * <p>
 * Wicketの<b>developmentモードの時にのみ</b>以下の機能が有効になります。
 * </p>
 * <dl>
 * <dt>クラスの自動リロード(HotDeploy)</dt>
 * <dd>web.xmlの初期化パラメータでreloadingClassPatternに指定したクラスが対象になります。</dd>
 * <dt>クラスリロード時の自動セッション無効化</dt>
 * <dd>クラスリロード時に全クラスローダで読み込まれたセッション上のインスタンスを破棄します。
 * クラスローダの違いによってセッションでエラーが出るのを回避します。</dd>
 * </dl>
 * <h4>初期化パラメータ</h4>
 * <dl>
 * <dt>applicationClassName</dt>
 * <dd>WicketのWebApplicationクラスの実装を指定します。</dd>
 * <dt>configuration</dt>
 * <dd>Wicketのコンフィギュレーションを「deployment」（配備時）もしくは「development」（開発時）で指定します。
 * 指定しない場合はdevelopment(開発モード)となります。</dd>
 * <dt>debug</dt>
 * <dd>S2Containerの状態を表示するページのパスを指定します。</dd>
 * <dt>reloadingClassPattern</dt>
 * <dd>Wicketのコンフィギュレーションがdevelopmentの時にリロード対象とするクラスを指定します。
 * ここで、指定しなければクラスの自動リロードは行われません。","区切りによる複数クラスの指定、
 * および"*"によるワイルドカード指定が可能です。また、クラスパターンの頭に"-"をつけることで、
 * リロード対象から除外することができます。通常はconvention.diconで追加したrootPackageNameを指定します。</dd>
 * <dl>
 * <h4>web.xml例</h4>
 * 
 * <pre>
 * &lt;filter&gt;
 *   &lt;filter-name&gt;sawicketfilter&lt;/filter-name&gt;
 *   &lt;filter-class&gt;org.karatachi.sawicket.SAWicketFilter&lt;/filter-class&gt;
 *   &lt;init-param&gt;
 *     &lt;!-- Wicketのアプリケーションクラス --&gt;
 *     &lt;param-name&gt;applicationClassName&lt;/param-name&gt;
 *     &lt;param-value&gt;org.karatachi.example.ExampleApplication&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;!-- Wicketの配備モード（デフォルトはdevelopmentモード） --&gt;
 *     &lt;!-- developmentモード時のみクラスの自動リロードが行われる --&gt;
 *     &lt;param-name&gt;configuration&lt;/param-name&gt;
 *     &lt;param-value&gt;deployment&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;!-- S2Containerのデバッグ出力を行うパスを指定 --&gt;
 *     &lt;param-name&gt;debug&lt;/param-name&gt;
 *     &lt;param-value&gt;/debug&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;!-- リロードを行う対象となるクラス（developmentモード時のみ有効） --&gt;
 *     &lt;param-name&gt;reloadingClassPattern&lt;/param-name&gt;
 *     &lt;param-value&gt;org.karatachi.example.*&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/filter&gt;
 * &lt;filter-mapping&gt;
 *   &lt;filter-name&gt;sawicketfilter&lt;/filter-name&gt;
 *   &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 * &lt;/filter-mapping&gt;
 * </pre>
 * 
 * @author TAKEUCHI Hideyuki (chimerast)
 */
public class SAWicketFilter extends ReloadingWicketFilter {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // セッションクラスローダのタグ
    private static final String SESSION_LOADER = "sawicket$loader";

    /** Wicketのコンフィグ */
    private String configuration;

    /** S2Containerでロードするdiconファイルのパス(デフォルト: app.dicon) */
    private String configPath;
    /** S2Containerの状態出力ページのURLパス */
    private String debug;
    /** Hot Deploy を行うクラスのパターン(WicketのMatcherに依存) */
    private String reloadingClassPattern;

    /** アプリケーションのコンフィグ(DEPLOYMENT, DEVELOPMENT) */
    private String applicationConfigType;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (SingletonS2ContainerFactory.hasContainer()) {
            SingletonS2ContainerFactory.destroy();
        }

        // コンフィギュレーションの読み取り
        configuration =
                getInitParameter(filterConfig, "configuration", "development");
        configPath = getInitParameter(filterConfig, "configPath", "app.dicon");
        debug = getInitParameter(filterConfig, "debug", null);
        reloadingClassPattern =
                getInitParameter(filterConfig, "reloadingClassPattern", null);

        if (logger.isInfoEnabled()) {
            logger.info("[config] configuration='{}'", configuration);
            logger.info("[config] configPath='{}'", configPath);
            logger.info("[config] debug='{}'", debug);
            logger.info("[config] reloadingClassPattern='{}'",
                    reloadingClassPattern);
        }

        if (!Application.DEPLOYMENT.equalsIgnoreCase(configuration)
                && reloadingClassPattern != null) {
            ReloadingClassLoader.getPatterns().clear();
            // すべてのクラスが読み込まれる前に先に監視クラスを設定
            // これ以前にロードされたクラスについては監視対象から外れる
            for (String classPattern : reloadingClassPattern.split(",")) {
                if (!classPattern.startsWith("-")) {
                    ReloadingClassLoader.includePattern(classPattern);
                } else {
                    ReloadingClassLoader.excludePattern(classPattern.substring(1));
                }
            }
            for (URL str : ReloadingClassLoader.getLocations()) {
                logger.info("[classpath] {}", str);
            }
            for (String str : ReloadingClassLoader.getPatterns()) {
                logger.info("[pattern] {}", str);
            }
        }

        ComponentDeployerFactory.setProvider(new ExternalComponentDeployerProvider());
        S2Container s2container =
                S2ContainerFactory.create(configPath, getClassLoader());
        s2container.setExternalContext(new HttpServletExternalContext());
        s2container.setExternalContextComponentDefRegister(new HttpServletExternalContextComponentDefRegister());
        s2container.getExternalContext().setApplication(
                filterConfig.getServletContext());
        s2container.init();
        SingletonS2ContainerFactory.setContainer(s2container);

        if (SmartDeployUtil.isHotdeployMode(SingletonS2ContainerFactory.getContainer())) {
            throw new ServletException(
                    "SAWicket does not support HOT deploy mode.");
        }

        super.init(filterConfig);

        // 関連づけられたWebApplicationを取り出す（現状これしか方法がない？）
        String contextKey = "wicket:" + filterConfig.getFilterName();
        WebApplication webApplication =
                (WebApplication) filterConfig.getServletContext().getAttribute(
                        contextKey);
        webApplication.addComponentInstantiationListener(new ComponentInjectionListener());
        applicationConfigType = webApplication.getConfigurationType();
    }

    @Override
    public void destroy() {
        if (SingletonS2ContainerFactory.hasContainer()) {
            SingletonS2ContainerFactory.destroy();
        }
        super.destroy();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        if (Application.DEVELOPMENT.equalsIgnoreCase(applicationConfigType)) {
            if (request instanceof HttpServletRequest) {
                // 旧セッションクラスローダーで読み込まれていたセッションオブジェクトの削除
                HttpSession session =
                        ((HttpServletRequest) request).getSession();
                ClassLoader previousLoader =
                        (ClassLoader) session.getAttribute(SESSION_LOADER);
                if (previousLoader != getClassLoader()) {
                    logger.info("[reload] invalidate old session attributes ...");
                    Enumeration<String> names = session.getAttributeNames();
                    while (names.hasMoreElements()) {
                        String name = names.nextElement();
                        Object obj = session.getAttribute(name);
                        ClassLoader objectLoader =
                                obj != null ? obj.getClass().getClassLoader()
                                        : null;
                        if (previousLoader == null
                                || objectLoader == previousLoader) {
                            session.removeAttribute(name);
                        }
                    }
                }
                session.setAttribute(SESSION_LOADER, getClassLoader());
            }

            // デバッグ出力
            if (debug != null) {
                HttpServletRequest req = (HttpServletRequest) request;
                HttpServletResponse res = (HttpServletResponse) response;
                if (debug.equals(req.getServletPath())) {
                    SAWicketDebugBuilder builder = new SAWicketDebugBuilder();
                    builder.doResponse(req, res);
                    return;
                }
            }
        }

        // S2ContainerFilterの処理と同じ
        S2Container container = SingletonS2ContainerFactory.getContainer();
        ExternalContext externalContext = container.getExternalContext();
        if (externalContext == null) {
            throw new EmptyRuntimeException("externalContext");
        }
        externalContext.setRequest(request);
        externalContext.setResponse(response);

        try {
            super.doFilter(request, response, chain);
        } finally {
            externalContext.setRequest(null);
            externalContext.setResponse(null);
        }
    }

    /*
     * デフォルト値付き初期化パラメータ取得のためのユーティリティ関数
     */
    private String getInitParameter(FilterConfig filterConfig, String key,
            String def) {
        String value = filterConfig.getInitParameter(key);
        return value != null ? value : def;
    }
}
