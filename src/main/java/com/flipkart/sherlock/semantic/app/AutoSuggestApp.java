package com.flipkart.sherlock.semantic.app;


import com.flipkart.sherlock.semantic.autosuggest.providers.JsonSeDeProvider;
import com.flipkart.sherlock.semantic.autosuggest.views.AutoSuggestView;
import com.flipkart.sherlock.semantic.common.dao.mysql.entity.MysqlConfig;
import com.flipkart.sherlock.semantic.common.dao.mysql.entity.MysqlConnectionPoolConfig;
import com.flipkart.sherlock.semantic.core.augment.init.MysqlDaoProvider;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by anurag.laddha on 02/04/17.
 */
public class AutoSuggestApp {

    public static void main(String[] args) throws Exception {

        MysqlConfig mysqlConfig = new MysqlConfig("localhost", 3306, "root", "", "sherlock");
        MysqlConnectionPoolConfig connectionPoolConfig = new MysqlConnectionPoolConfig.MysqlConnectionPoolConfigBuilder(1, 10)
                .setInitialPoolSize(1)
                .setAcquireIncrement(2)
                .setMaxIdleTimeSec((int) TimeUnit.MINUTES.toSeconds(30)).build();

        Injector injector = Guice.createInjector(
                new MysqlDaoProvider(mysqlConfig, connectionPoolConfig),
                new JsonSeDeProvider());

        // By default, jetty task queue is unbounded. Reject requests once queue is full.
        QueuedThreadPool threadPool = new QueuedThreadPool(1024, 8, (int) TimeUnit.MINUTES.toMillis(1), new ArrayBlockingQueue<Runnable>(1024));
        threadPool.setName("JettyContainer");

        // Create embedded jetty container
        Server server = new Server(threadPool);

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(9001);
        connector.setName("AutoSuggestApplication");
        server.addConnector(connector);

        ContextHandlerCollection contexts = new ContextHandlerCollection();

        AutoSuggestView autoSuggestView = injector.getInstance(AutoSuggestView.class);

        ResourceConfig resourceConfig = new ResourceConfig().register(autoSuggestView);

        ServletContextHandler contextDefault = new ServletContextHandler();
        contextDefault.setContextPath("/");
        contextDefault.addServlet(new ServletHolder(new ServletContainer(resourceConfig)), "/*");
        contextDefault.setVirtualHosts(new String[]{"@AutoSuggestApplication"});
        contexts.addHandler(contextDefault);
        server.setHandler(contexts);

        try {
            server.start();
            server.join();
        } finally {
            server.stop();
        }
    }
}
