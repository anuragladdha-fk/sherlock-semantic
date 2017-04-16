package com.flipkart.sherlock.semantic.util;

/**
 * Created by anurag.laddha on 16/04/17.
 */

import com.flipkart.sherlock.semantic.dao.mysql.entity.MysqlConfig;
import com.flipkart.sherlock.semantic.dao.mysql.entity.MysqlConnectionPoolConfig;
import com.flipkart.sherlock.semantic.init.MiscInitProvider;
import com.flipkart.sherlock.semantic.init.MysqlDaoProvider;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import java.util.concurrent.TimeUnit;

/**
 * Use this context to get object instances for testing
 * This must be used for integration tests ONLY
 */

public class TestContext {
    private static Injector injector;

    private static void init() {
        if (injector == null) {
            synchronized (TestContext.class) {
                if (injector == null) {
                    MysqlConfig mysqlConfig = new MysqlConfig("localhost", 3306, "root", "", "sherlock");
                    MysqlConnectionPoolConfig connectionPoolConfig = new MysqlConnectionPoolConfig.MysqlConnectionPoolConfigBuilder(1,10)
                        .setInitialPoolSize(1)
                        .setAcquireIncrement(2)
                        .setMaxIdleTimeSec((int) TimeUnit.MINUTES.toSeconds(30)).build();

                    injector = Guice.createInjector(new MysqlDaoProvider(mysqlConfig, connectionPoolConfig),
                        new MiscInitProvider(3, 5));
                }
            }
        }
    }


    public static <T> T getInstance(Class<T> klass){
        init();
        return injector.getInstance(klass);
    }

    public static <T> T getInstance(Class<T> klass, String namedAnnotation){
        init();
        return injector.getInstance(Key.get(klass, Names.named(namedAnnotation)));
    }
}
