package org.seasar.wicket.example.logic;

import java.util.Date;
import java.util.List;

import org.seasar.framework.container.annotation.tiger.Binding;
import org.seasar.wicket.example.dao.MessageDao;
import org.seasar.wicket.example.entity.Message;

public class BoardLogic {
    @Binding
    MessageDao messageDao;

    public List<Message> getMessages() {
        return messageDao.select();
    }

    public void response(String name, String message) {
        Message entity = new Message();
        entity.name = name;
        entity.message = message;
        entity.date = new Date();
        messageDao.insert(entity);
    }
}
