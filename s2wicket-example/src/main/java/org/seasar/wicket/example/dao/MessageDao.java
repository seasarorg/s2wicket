package org.seasar.wicket.example.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.wicket.example.entity.Message;

@S2Dao(bean = Message.class)
public interface MessageDao {
    public List<Message> select();

    public void insert(Message message);
}
