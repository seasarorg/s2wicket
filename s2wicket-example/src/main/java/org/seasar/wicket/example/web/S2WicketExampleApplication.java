package org.seasar.wicket.example.web;

import java.io.InputStreamReader;
import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.wicket.Page;
import org.h2.tools.RunScript;
import org.seasar.framework.container.SingletonS2Container;
import org.seasar.wicket.S2WebApplication;
import org.seasar.wicket.example.web.index.IndexPage;

public class S2WicketExampleApplication extends S2WebApplication {
    @Override
    protected void init() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        // オンメモリデータベースのテーブル作成
        DataSource dataSource =
                SingletonS2Container.getComponent(DataSource.class);
        try {
            Connection connection = dataSource.getConnection();
            RunScript.execute(connection,
                    new InputStreamReader(
                            getClass().getClassLoader().getResourceAsStream(
                                    "init.sql"), "UTF-8"));
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return IndexPage.class;
    }
}
