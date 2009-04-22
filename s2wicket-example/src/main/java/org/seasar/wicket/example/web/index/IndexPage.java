package org.seasar.wicket.example.web.index;

import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.seasar.wicket.example.entity.Message;
import org.seasar.wicket.example.logic.BoardLogic;

public class IndexPage extends WebPage {
    private static final long serialVersionUID = 1L;

    @Binding
    private BoardLogic boardLogic;

    public IndexPage() {
        // 書き込みフォーム
        add(new IndexPageForm("form"));

        // メッセージリスト
        add(new ListView<Message>("messages", new PropertyModel<List<Message>>(
                this, "boardLogic.messages")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Message> item) {
                Message message = item.getModelObject();
                item.add(new Label("name", message.name));
                item.add(new Label("date", message.date.toString()));
                item.add(new Label("message", message.message));
            }
        });
    }

    private class IndexPageForm extends Form<IndexPageForm> {
        private static final long serialVersionUID = 1L;

        private String name;
        private String message;

        public IndexPageForm(String id) {
            super(id);
            setDefaultModel(new CompoundPropertyModel<IndexPageForm>(this));
            add(new TextField<String>("name"));
            add(new TextField<String>("message"));
        }

        @Override
        protected void onSubmit() {
            boardLogic.response(name, message);
        }
    }
}
