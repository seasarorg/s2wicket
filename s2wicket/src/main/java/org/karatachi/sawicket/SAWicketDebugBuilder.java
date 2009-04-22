package org.karatachi.sawicket;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.framework.aop.impl.PointcutImpl;
import org.seasar.framework.container.ArgDef;
import org.seasar.framework.container.ArgDefAware;
import org.seasar.framework.container.AspectDef;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.ContainerNotRegisteredRuntimeException;
import org.seasar.framework.container.Expression;
import org.seasar.framework.container.MethodDef;
import org.seasar.framework.container.PropertyDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.container.impl.ArgDefImpl;
import org.seasar.framework.container.util.SmartDeployUtil;
import org.seasar.framework.env.Env;
import org.seasar.framework.util.StringUtil;
import org.seasar.framework.util.URLUtil;

/**
 * S2Containerのデバッグ情報のHTMLを出力するビルダ。
 * <p>
 * S2ContainerServletのデバッグ出力部のコピーです。
 * </p>
 * 
 * @author TAKEUCHI Hideyuki (chimerast)
 */
public class SAWicketDebugBuilder {
    private static final String MODE_BEGIN = "<strong><font color='#DC143C'>";
    private static final String MODE_END = "</font></strong>";

    public static final String COMMAND = "command";
    public static final String RESTART = "restart";
    public static final String LIST = "list";
    public static final String PATH = "path";

    private final S2Container s2container;

    public SAWicketDebugBuilder() {
        this.s2container = SingletonS2ContainerFactory.getContainer();
    }

    public void doResponse(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String command = request.getParameter(COMMAND);
        if (RESTART.equalsIgnoreCase(command)) {
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("S2ContainerServlet is restarted.");
        } else if (LIST.equalsIgnoreCase(command)) {
            response.setContentType("text/html; charset=UTF-8");
            list(request, response);
        } else {
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("S2WicketFilter is running.");
        }
    }

    /**
     * {@link S2Container}の中身({@link ComponentDef})を表示します。
     * 
     * @param request
     * @param response
     * @throws IOException
     */
    protected void list(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        final PrintWriter out = response.getWriter();

        final String path = request.getParameter(PATH);
        final S2Container container = getContainer(path);
        if (container == null) {
            out.write("S2Container[" + escape(path) + "] is not found.");
            return;
        }
        out.write("<html><head><title>Seasar2 Component List</title></head><body>");
        try {
            out.write("<h1>S2Container</h1>");
            printSmartDeploy(container, out);
            out.write("<ul>");
            try {
                out.write("<li>path : <code>" + escape(container.getPath())
                        + "</code></li>");
                out.write("<li>classLoader : <code>"
                        + escape(container.getClassLoader().getClass().getName())
                        + "</code></li>");
                final String nameSpace = container.getNamespace();
                if (!StringUtil.isEmpty(nameSpace)) {
                    out.write("<li>namespace : <code>" + escape(nameSpace)
                            + "</code></li>");
                }
                final String envValue = Env.getValue();
                if (!StringUtil.isEmpty(envValue)) {
                    out.write("<li>env : <code>" + escape(envValue)
                            + "</code></li>");
                }
            } finally {
                out.write("</ul>");
            }
            listInclude(container, request, out);
            listComponent(container, out);
        } finally {
            out.write("</body></html>");
        }
    }

    /**
     * {@link S2Container}を返します。
     * 
     * @param path
     * @return {@link S2Container}
     */
    protected S2Container getContainer(final String path) {
        final S2Container root = s2container;
        try {
            return StringUtil.isEmpty(path) ? root : root.getDescendant(path);
        } catch (final ContainerNotRegisteredRuntimeException e) {
            return null;
        }
    }

    private void printSmartDeploy(final S2Container container,
            final PrintWriter out) {
        out.write("<p>S2Container is working under ");
        try {
            if (SmartDeployUtil.isHotdeployMode(container)) {
                out.write(MODE_BEGIN + "HOT deploy" + MODE_END);
            } else if (SmartDeployUtil.isWarmdeployMode(container)) {
                out.write(MODE_BEGIN + "WARM deploy" + MODE_END);
            } else if (SmartDeployUtil.isCooldeployMode(container)) {
                out.write(MODE_BEGIN + "COOL deploy" + MODE_END);
            } else {
                out.write("normal");
            }
        } finally {
            out.write(" mode.</p>");
        }
    }

    private void listInclude(final S2Container container,
            final HttpServletRequest request, final PrintWriter out)
            throws IOException {
        if (container.getChildSize() == 0) {
            return;
        }
        out.write("<h2>Includes</h2>");
        out.write("<p><ul>");
        try {
            final String requestUri = request.getRequestURI();
            final String queryString =
                    "?" + COMMAND + "=" + LIST + "&" + PATH + "=";
            for (int i = 0; i < container.getChildSize(); ++i) {
                final S2Container child = container.getChild(i);
                final String path = child.getPath();
                out.write("<li><a href='" + requestUri + queryString
                        + URLUtil.encode(path, "UTF-8") + "'><code>" + path
                        + "</code></a></li>");
            }
        } finally {
            out.write("</ul></p>");
        }
    }

    private void listComponent(final S2Container container,
            final PrintWriter out) throws IOException {
        if (container.getComponentDefSize() == 0) {
            return;
        }
        out.write("<h2>Components</h2>");
        out.write("<p><ul>");
        try {
            for (int i = 0; i < container.getComponentDefSize(); ++i) {
                final ComponentDef cd = container.getComponentDef(i);
                printComponent(cd, out);
            }
        } finally {
            out.write("</ul></p>");
        }
    }

    private void printComponent(final ComponentDef cd, final PrintWriter out)
            throws IOException {
        final String name = cd.getComponentName();
        final Class<?> clazz = cd.getComponentClass();
        out.write("<li style='list-style-type: square'><code><strong>"
                + (name != null ? escape(name) : "-") + " ["
                + (clazz != null ? escape(clazz.getName()) : "-")
                + "]</strong></code>");
        out.write("<ul>");
        out.write("<li style='list-style-type: circle'>instance : <code>"
                + escape(cd.getInstanceDef().getName()) + "</code></li>");
        out.write("<li style='list-style-type: circle'>autoBinding : <code>"
                + escape(cd.getAutoBindingDef().getName()) + "</code></li>");

        Expression expression = cd.getExpression();
        final String expr = (expression != null) ? expression.toString() : "";
        if (!StringUtil.isEmpty(expr)) {
            out.write("<li style='list-style-type: circle'>ognl : <code>"
                    + escape(expr) + "</code></li>");
        }

        printArg(cd, out);
        printAspect(cd, out);
        printProperty(cd, out);
        printInitMethod(cd, out);
        printDestroyMethod(cd, out);

        try {
            final Object component = cd.getComponent();
            out.write("<li style='list-style-type: circle'>toString : <pre style='border-style: solid; border-width: 1'>"
                    + escape(component.toString()) + "</pre></li>");
        } catch (final Exception ignore) {
        }
        out.write("</ul>");
    }

    private void printArg(final ArgDefAware cd, final PrintWriter out)
            throws IOException {
        for (int i = 0; i < cd.getArgDefSize(); ++i) {
            out.write("<li style='list-style-type: circle'>arg<ul>");
            final ArgDef ad = cd.getArgDef(i);

            Expression expression = ad.getExpression();
            final String expr =
                    (expression != null) ? expression.toString() : "";
            if (!StringUtil.isEmpty(expr)) {
                out.write("<li style='list-style-type: circle'>ognl : <code>"
                        + escape(expr) + "</code></li>");
            }

            final ComponentDef child = getChildComponentDef(ad);
            if (child != null) {
                printComponent(child, out);
            }

            out.write("</ul></li>");
        }
    }

    private void printAspect(final ComponentDef cd, final PrintWriter out)
            throws IOException {
        for (int i = 0; i < cd.getAspectDefSize(); ++i) {
            out.write("<li style='list-style-type: circle'>aspect<ul>");
            final AspectDef ad = cd.getAspectDef(i);
            final PointcutImpl pc = (PointcutImpl) ad.getPointcut();
            if (pc != null) {
                final String[] pointCuts = pc.getMethodNames();
                if (pointCuts != null && pointCuts.length > 0) {
                    out.write("<li style='list-style-type: circle'>pointcut<ul>");
                    for (int j = 0; j < pointCuts.length; ++j) {
                        out.write("<li style='list-style-type: circle'><code>"
                                + escape(pointCuts[j]) + "</code></li>");
                    }
                    out.write("</ul></li>");
                }
            }

            Expression expression = ad.getExpression();
            final String expr =
                    (expression != null) ? expression.toString() : "";
            if (!StringUtil.isEmpty(expr)) {
                out.write("<li style='list-style-type: circle'>ognl : <code>"
                        + escape(expr) + "</code></li>");
            }

            final ComponentDef child = getChildComponentDef(ad);
            if (child != null) {
                printComponent(child, out);
            }

            out.write("</ul></li>");
        }
    }

    private void printProperty(final ComponentDef cd, final PrintWriter out)
            throws IOException {
        for (int i = 0; i < cd.getPropertyDefSize(); ++i) {
            out.write("<li style='list-style-type: circle'>property<ul>");
            final PropertyDef pd = cd.getPropertyDef(i);
            out.write("<li style='list-style-type: circle'>name : <code>"
                    + escape(pd.getPropertyName()) + "</code></li>");

            Expression expression = pd.getExpression();
            final String expr =
                    (expression != null) ? expression.toString() : "";
            if (!StringUtil.isEmpty(expr)) {
                out.write("<li style='list-style-type: circle'>ognl : <code>"
                        + escape(expr) + "</code></li>");
            }

            final ComponentDef child = getChildComponentDef(pd);
            if (child != null) {
                printComponent(child, out);
            }

            out.write("</ul></li>");
        }
    }

    private void printInitMethod(final ComponentDef cd, final PrintWriter out)
            throws IOException {
        for (int i = 0; i < cd.getInitMethodDefSize(); ++i) {
            out.write("<li style='list-style-type: circle'>initMethod<ul>");
            printMethod(cd.getInitMethodDef(i), out);
            out.write("</ul></li>");
        }
    }

    private void printDestroyMethod(final ComponentDef cd, final PrintWriter out)
            throws IOException {
        for (int i = 0; i < cd.getDestroyMethodDefSize(); ++i) {
            out.write("<li style='list-style-type: circle'>destroyMethod<ul>");
            printMethod(cd.getDestroyMethodDef(i), out);
            out.write("</ul></li>");
        }
    }

    private void printMethod(final MethodDef md, final PrintWriter out)
            throws IOException {
        out.write("<li style='list-style-type: circle'>name : <code>"
                + escape(md.getMethodName()) + "</code></li>");

        Expression expression = md.getExpression();
        final String expr = (expression != null) ? expression.toString() : "";
        if (!StringUtil.isEmpty(expr)) {
            out.write("<li style='list-style-type: circle'>ognl : <code>"
                    + escape(expr) + "</code></li>");
        }

        final ComponentDef child = getChildComponentDef(md);
        if (child != null) {
            printComponent(child, out);
        }
    }

    private ComponentDef getChildComponentDef(final Object o) {
        try {
            final Field f =
                    ArgDefImpl.class.getDeclaredField("childComponentDef");
            f.setAccessible(true);
            return (ComponentDef) f.get(o);
        } catch (final Exception e) {
            return null;
        }
    }

    private String escape(final String text) {
        if (text == null) {
            return "null";
        }
        final StringBuffer buf = new StringBuffer(text.length() * 4);
        for (int i = 0; i < text.length(); ++i) {
            final char ch = text.charAt(i);
            switch (ch) {
            case '<':
                buf.append("&lt;");
                break;
            case '>':
                buf.append("&gt;");
                break;
            case '&':
                buf.append("&amp;");
                break;
            case '"':
                buf.append("&quot;");
                break;
            default:
                buf.append(ch);
                break;
            }
        }
        return new String(buf);
    }
}
