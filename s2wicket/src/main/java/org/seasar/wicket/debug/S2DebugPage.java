package org.seasar.wicket.debug;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.model.CompoundPropertyModel;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.container.util.SmartDeployUtil;
import org.seasar.framework.env.Env;
import org.seasar.framework.util.StringUtil;

public class S2DebugPage extends WebPage {
    private static final long serialVersionUID = 1L;

    public S2DebugPage() {
        setDefaultModel(new CompoundPropertyModel<S2DebugPage>(this));

        add(new Label("env", Env.getValue()));
        add(new S2ContainerFragment("root",
                SingletonS2ContainerFactory.getContainer(), 0));
    }

    private class S2ContainerFragment extends Fragment implements
            IComponentResolver {
        private static final long serialVersionUID = 1L;

        public S2ContainerFragment(String id, final S2Container container,
                final int level) {
            super(id, "s2container", S2DebugPage.this,
                    new CompoundPropertyModel<S2Container>(container));

            add(new Label("deploymode", getSmartDeployMode(container)));
            add(new Label("namespace",
                    StringUtil.isEmpty(container.getNamespace()) ? "<none>"
                            : container.getNamespace()));

            add(new AutoResolveLoop("components",
                    container.getComponentDefSize()) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void populateItem(LoopItem item) {
                    item.setDefaultModel(new CompoundPropertyModel<ComponentDef>(
                            container.getComponentDef(item.getIndex())));
                }
            }.setVisible(container.getComponentDefSize() > 0));
            add(new AutoResolveLoop("containers", container.getChildSize()) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void populateItem(LoopItem item) {
                    item.add(AttributeModifier.replace("class", "level"
                            + (level + 1)));
                    item.add(new S2ContainerFragment("container",
                            container.getChild(item.getIndex()), level + 1));
                }
            }.setVisible(container.getChildSize() > 0));
        }

        public Component resolve(MarkupContainer container,
                MarkupStream markupStream, ComponentTag tag) {
            if (tag.isAutoComponentTag()) {
                return null;
            }
            Component component = new Label(tag.getId());
            container.autoAdd(component, markupStream);
            return component;
        }
    }

    private static abstract class AutoResolveLoop extends Loop implements
            IComponentResolver {
        private static final long serialVersionUID = 1L;

        public AutoResolveLoop(String id, int iterations) {
            super(id, iterations);
        }

        public Component resolve(MarkupContainer container,
                MarkupStream markupStream, ComponentTag tag) {
            if (tag.isAutoComponentTag()) {
                return null;
            }
            Component component = new Label(tag.getId());
            container.autoAdd(component, markupStream);
            return component;
        }
    }

    public String getSmartDeployMode(S2Container container) {
        if (SmartDeployUtil.isHotdeployMode(container)) {
            return "HOT deploy";
        }
        if (SmartDeployUtil.isWarmdeployMode(container)) {
            return "WARM deploy";
        }
        if (SmartDeployUtil.isCooldeployMode(container)) {
            return "COOL deploy";
        } else {
            return "normal";
        }
    }
}
