package com.flipkart.sherlock.semantic.init;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.flipkart.sherlock.semantic.config.Constants;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by anurag.laddha on 16/04/17.
 */
public class MiscInitProvider extends AbstractModule {

    private int cacheExpireSec;
    private int localCacheLoadingMaxThreads;

    public MiscInitProvider(int localCacheExpireSec, int localCacheLoadingMaxThreads){
        this.cacheExpireSec = localCacheExpireSec;
        this.localCacheLoadingMaxThreads = localCacheLoadingMaxThreads;
    }

    @Override
    protected void configure() {
        bind(Integer.class)
            .annotatedWith(Names.named(Constants.GUICE_LOCAL_CACHE_EXPIRY))
            .toInstance(this.cacheExpireSec);
    }

    @Named(Constants.GUICE_LOCAL_CACHE_LOADING_EXECUTOR_SERVICE)
    @Provides @Singleton
    ExecutorService getCacheLoadingExecutorService(){
        return Executors.newFixedThreadPool(this.localCacheLoadingMaxThreads);
    }

    @Provides @Singleton
    ObjectMapper getObjectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }
}
