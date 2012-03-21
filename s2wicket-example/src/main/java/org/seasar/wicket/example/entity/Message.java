package org.seasar.wicket.example.entity;

import java.io.Serializable;
import java.util.Date;

import org.seasar.dao.annotation.tiger.Bean;

@Bean(table = "message")
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public Date date;
    public String message;
}
