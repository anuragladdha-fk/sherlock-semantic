package com.flipkart.sherlock.semantic.app;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by anurag.laddha on 02/04/17.
 */
public class SemanticApp {

    public static void main(String[] args) throws Exception {

        //TODO get details from config service

        QueuedThreadPool threadPool = new QueuedThreadPool(1024, 8, (int) TimeUnit.MINUTES.toMillis(1),
            new ArrayBlockingQueue<Runnable>(1024));   //By default, jetty task queue is unbounded. Reject requests once queue is full.
        threadPool.setName("Jetty-container");

        Server server = new Server(threadPool);   //Create embedded jetty container
        ServerConnector httpConnector = new ServerConnector(server);
        httpConnector.setPort(9001);
        server.addConnector(httpConnector);

        String webxmlLocation = SemanticApp.class.getResource("/web/WEB-INF/web.xml").toString();
        String resLocation = SemanticApp.class.getResource("/web").toString();

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");  //Handler to handle all requests from root
        webAppContext.setDescriptor(webxmlLocation);
        webAppContext.setResourceBase(resLocation);
        webAppContext.setParentLoaderPriority(true);

        server.setHandler(webAppContext);

        try {
            server.start();
            server.join();
        }
        finally{
            server.stop();
        }
    }
}
