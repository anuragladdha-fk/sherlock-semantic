package com.flipkart.sherlock.semantic.common.solr;

import com.flipkart.sherlock.semantic.common.config.Constants;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrServer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by anurag.laddha on 26/04/17.
 */
@Slf4j
@Singleton
public class AutoSuggestSolrServerProvider {

    private LoadingCache<Core, FKHttpSolServer> coreSolrServerCache;

    @Inject
    public AutoSuggestSolrServerProvider(@Named(Constants.GUICE_LOCAL_CACHE_LOADING_EXECUTOR_SERVICE) ExecutorService executorService) {
        SolrCoreLoader solrCoreLoader = new SolrCoreLoader(executorService);
        coreSolrServerCache = CacheBuilder.newBuilder().maximumSize(10).refreshAfterWrite(600, TimeUnit.SECONDS)
                .build(solrCoreLoader);
    }

    public SolrServer getSolrServer(Core core) {
        try {
            return coreSolrServerCache.get(core);
        } catch (ExecutionException e) {
            log.error("Unable to get SolrServer from cache", e);
        }
        return new FKHttpSolServer(core.getSolrUrl());
    }

    @AllArgsConstructor
    static class SolrCoreLoader extends CacheLoader<Core, FKHttpSolServer> {

        private ExecutorService executorService;

        @Override
        public ListenableFuture<FKHttpSolServer> reload(Core core, FKHttpSolServer oldFkHttpSolServer) {

            log.info("Queuing request to fetch solr server core {} async", core.getSolrUrl());

            ListenableFutureTask<FKHttpSolServer> task = ListenableFutureTask.create(() -> {
                log.info("Reloading core {} in cache", core.getSolrUrl());
                FKHttpSolServer fkHttpSolServer = new FKHttpSolServer(core.getSolrUrl());
                log.info("Completed reloading core {} in cache", core.getSolrUrl());
                return fkHttpSolServer;
            });

            this.executorService.submit(task);
            return task;
        }

        @Override
        public FKHttpSolServer load(Core core) {
            log.info("Instantiating core {} in cache", core.getSolrUrl());
            return new FKHttpSolServer(core.getSolrUrl());
        }
    }
}
