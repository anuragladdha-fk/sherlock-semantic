package com.flipkart.sherlock.semantic.core.augment.init;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.flipkart.sherlock.semantic.common.config.Constants;
import com.flipkart.sherlock.semantic.core.augment.*;
import com.flipkart.sherlock.semantic.common.flow.IStage;
import com.flipkart.sherlock.semantic.common.flow.Stage;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
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

        /**
         * Binder for flavor of stage type to implementation of that stage
         */

        MapBinder<Stage.Flavor, IStage> stageFlavorToStageImplMap =
            MapBinder.newMapBinder(binder(), Stage.Flavor.class, IStage.class);

        stageFlavorToStageImplMap.addBinding(Stage.Flavor.TermAlternativesDefault).to(DefaultTermAlternativeStage.class)
            .in(Singleton.class);


        /**
         * Binder for flavor of negatives data source type to concrete implementation
         */
        MapBinder<IDataNegatives.Type, IDataNegatives> negativesDataSourceTypeToImplMap =
            MapBinder.newMapBinder(binder(), IDataNegatives.Type.class, IDataNegatives.class);
        negativesDataSourceTypeToImplMap.addBinding(IDataNegatives.Type.Default).to(CachedNegativesDataSource.class).in(Singleton.class);


        /**
         * Binder for flavor of term alternatives data source type to concrete implementation
         */
        MapBinder<IDataTermAlternatives.Type, IDataTermAlternatives> termAlternativesDataSourceTypeToImplMap =
            MapBinder.newMapBinder(binder(), IDataTermAlternatives.Type.class, IDataTermAlternatives.class);
        termAlternativesDataSourceTypeToImplMap.addBinding(IDataTermAlternatives.Type.Default).to(LocalCachedTermAlternativesDataSource.class)
            .in(Singleton.class);
    }

    @Named(Constants.GUICE_LOCAL_CACHE_LOADING_EXECUTOR_SERVICE)
    @Provides
    @Singleton
    ExecutorService getCacheLoadingExecutorService(){
        return Executors.newFixedThreadPool(this.localCacheLoadingMaxThreads);
    }

    @Provides
    @Singleton
    ObjectMapper getObjectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }

    @Provides
    @Singleton
    @Inject
    DefaultTermAlternativeStage getDefaultTermAlternativeStage(TermAlternativesService termAlternativesService){
        return new DefaultTermAlternativeStage(Stage.Type.TermAlternatives, termAlternativesService);
    }
}
