package com.flipkart.sherlock.semantic.app;


import com.flipkart.sherlock.semantic.dao.mysql.entity.MysqlConfig;
import com.flipkart.sherlock.semantic.dao.mysql.entity.MysqlConnectionPoolConfig;
import com.flipkart.sherlock.semantic.init.MiscInitProvider;
import com.flipkart.sherlock.semantic.init.MysqlDaoProvider;
import com.flipkart.sherlock.semantic.resources.TestDaoResource;
import com.flipkart.sherlock.semantic.resources.TestResource;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
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
public class SemanticApp {

    public static void main(String[] args) throws Exception {

        //TODO get details from config service.

        MysqlConfig mysqlConfig = new MysqlConfig("localhost", 3306, "root", "", "sherlock");
        MysqlConnectionPoolConfig connectionPoolConfig = new MysqlConnectionPoolConfig.MysqlConnectionPoolConfigBuilder(1,10)
            .setInitialPoolSize(1)
            .setAcquireIncrement(2)
            .setMaxIdleTimeSec((int) TimeUnit.MINUTES.toSeconds(30)).build();

        Injector injector = Guice.createInjector(new MysqlDaoProvider(mysqlConfig, connectionPoolConfig),
            new MiscInitProvider((int) TimeUnit.MINUTES.toSeconds(30), 10));
        TestResource testResource = injector.getInstance(TestResource.class);
        TestDaoResource testDaoResource = injector.getInstance(TestDaoResource.class);

        QueuedThreadPool threadPool = new QueuedThreadPool(1024, 8, (int) TimeUnit.MINUTES.toMillis(1),
            new ArrayBlockingQueue<Runnable>(1024));   //By default, jetty task queue is unbounded. Reject requests once queue is full.
        threadPool.setName("Jetty-container");

        Server server = new Server(threadPool);   //Create embedded jetty container
        ServerConnector httpConnector = new ServerConnector(server);
        httpConnector.setPort(9001);
        server.addConnector(httpConnector);

        //Add resource classes explicitly. Cannot read from web.xml since we are using Guice DI.
        ResourceConfig resourceConfig = new ResourceConfig()
            .register(testResource)
            .register(testDaoResource);

        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("/");
        handler.addServlet(new ServletHolder(new ServletContainer(resourceConfig)), "/*");
        server.setHandler(handler);

        try {
            server.start();
            server.join();
        }
        finally{
            server.stop();
        }
    }
}
