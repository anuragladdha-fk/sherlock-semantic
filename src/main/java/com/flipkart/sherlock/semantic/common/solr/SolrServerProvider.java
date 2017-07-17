package com.flipkart.sherlock.semantic.common.solr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flipkart.sherlock.semantic.common.config.Constants;
import com.flipkart.sherlock.semantic.common.config.SearchConfigProvider;
import com.flipkart.sherlock.semantic.common.util.SerDeUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by anurag.laddha on 26/04/17.
 */

@Slf4j
@Singleton
public class SolrServerProvider {

//    private static final Logger log = LoggerFactory.getLogger(SolrUtilProvider.class);

    private Map<String, SolrServer> solrServerMap = new HashMap<>();
    private LoadingCache<String, Map<ExperimentCore, FKHttpSolServer>> experimentCoreToSolrServerCache;
    private SearchConfigProvider searchConfigProvider;
    private Map<String, String> coreToUrlMap;
    private static final Object syncObj = new Object();
    private static TypeReference<Map<String, String>> strToStrMap = new TypeReference<Map<String, String>>() {
    };

    @Inject
    public SolrServerProvider(SearchConfigProvider searchConfigProvider,
                              @Named(Constants.GUICE_LOCAL_CACHE_LOADING_EXECUTOR_SERVICE) ExecutorService executorService) {
        this.searchConfigProvider = searchConfigProvider;
        ExperimentSolrCoreLoader experimentSolrCoreLoader = new ExperimentSolrCoreLoader(searchConfigProvider, executorService);
        this.experimentCoreToSolrServerCache = CacheBuilder.newBuilder().maximumSize(10).refreshAfterWrite(120, TimeUnit.SECONDS)
                .build(experimentSolrCoreLoader);
        this.coreToUrlMap = SerDeUtils.getValueOrDefault(this.searchConfigProvider.getSearchConfig("coreToUrlMap",
                strToStrMap), Collections.emptyMap());
        ;
    }

    public SolrServer getSolrServer(String coreName, String experimentName) {
        if (experimentName == null || experimentName.isEmpty())
            return getSolrServer(coreName);
        SolrServer solrServer = null;
        if (StringUtils.isNotBlank(coreName)) {
            try {
                //Evaluate if there is a different solr server to be used for given core and experiment
                Boolean dbSwitch = SerDeUtils.getValueOrDefault(this.searchConfigProvider.getSearchConfig("TurnOnIntentExperiment",
                        Boolean.class), Boolean.FALSE);
                if (dbSwitch && StringUtils.isNotBlank(experimentName) && !Constants.CONTEXT_DEFAULT.equalsIgnoreCase(experimentName)) {
                    Map<ExperimentCore, FKHttpSolServer> allExpToSolrServerMap = this.experimentCoreToSolrServerCache.get(Constants.DUMMY_KEY);
                    ExperimentCore experimentCore = new ExperimentCore(coreName, experimentName);
                    if (allExpToSolrServerMap.containsKey(experimentCore)) {
                        solrServer = allExpToSolrServerMap.get(experimentCore);
                    }
                }
            } catch (Exception ex) {
                log.error("Error in getting solr server for given core name: {} and experiment: {}", coreName, experimentName, ex);
            }
            //Default solr server for given core
            solrServer = solrServer == null ? getSolrServer(coreName) : solrServer;
            return solrServer;
        }
        return null;
    }

    /**
     * Creates solr server instance from solr core to url mapping and cache's solr server instance
     */
    public SolrServer getSolrServer(String coreName) {
        if (!this.solrServerMap.containsKey(coreName)) {
            synchronized (syncObj) {
                if (!this.solrServerMap.containsKey(coreName) && this.coreToUrlMap.containsKey(coreName)) {
                    this.solrServerMap.put(coreName, new FKHttpSolServer(this.coreToUrlMap.get(coreName)));
                }
            }
        }
        return this.solrServerMap.get(coreName);
    }


    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    @ToString
    static class ExperimentCore {
        private String solrCore;
        private String experiment;
    }

    @AllArgsConstructor
    static class ExperimentSolrCoreLoader extends CacheLoader<String, Map<ExperimentCore, FKHttpSolServer>> {

        private static TypeReference<List<Map<String, Object>>> listOfStringObjMap = new TypeReference<List<Map<String, Object>>>() {
        };
        private SearchConfigProvider searchConfigProvider;
        private ExecutorService executorService;

        @Override
        public Map<ExperimentCore, FKHttpSolServer> load(String s) throws Exception {
            log.info("Fetching solr server core experiments");
            Map<ExperimentCore, FKHttpSolServer> exp = getAllExperimentalSolrServers();
            log.info("Finished fetching solr server core experiments. Number of entries: {}", exp != null ?
                    exp.size() : 0);
            return exp;
        }

        @Override
        public ListenableFuture<Map<ExperimentCore, FKHttpSolServer>> reload(String key, Map<ExperimentCore, FKHttpSolServer> oldValue) throws Exception {
            log.info("Queuing request to fetch solr server core experiments async");
            ListenableFutureTask<Map<ExperimentCore, FKHttpSolServer>> task = ListenableFutureTask.create(() -> {
                log.info("Fetching solr server core experiments async");
                Map<ExperimentCore, FKHttpSolServer> exp = getAllExperimentalSolrServers();
                log.info("Finished fetching solr server core experiments async. Number of entries: {}", exp != null ?
                        exp.size() : 0);
                return exp;
            });
            this.executorService.submit(task);
            return task;
        }

        @VisibleForTesting
        Map<ExperimentCore, FKHttpSolServer> getAllExperimentalSolrServers() {
            Map<ExperimentCore, FKHttpSolServer> coreExpToSolrServerMap = new HashMap<>();
            try {
                List<Map<String, Object>> experimentsCoreConfigList = this.searchConfigProvider.getSearchConfig("UtilCoreExperimentMapList",
                        listOfStringObjMap);
                experimentsCoreConfigList.forEach(expMap -> {
                    String host = String.valueOf(expMap.get("host"));
                    String port = String.valueOf(SerDeUtils.getValueOrDefault(expMap.get("port"), "25280")); //todo validate default port
                    String experimentName = String.valueOf(expMap.get("experiment"));
                    Map<String, String> coreRedirectionMap = (Map<String, String>) expMap.get("core");
                    //for all the solr core mappings in given experiment entry, create solr server
                    coreRedirectionMap.forEach((origCore, redirectedCore) -> {
                        ExperimentCore experimentCore = new ExperimentCore(origCore, experimentName);
                        if (coreExpToSolrServerMap.containsKey(experimentCore)) {
                            log.error("Core + Experiment has more than 1 mapping");
                        } else {
                            coreExpToSolrServerMap.put(experimentCore, new FKHttpSolServer(createSolrUrl(host, port, redirectedCore)));
                        }
                    });
                });
            } catch (Exception ex) {
                log.error("Error in loading solr core experiments", ex);
                //todo metric & alert
            }
            return coreExpToSolrServerMap;
        }

        @VisibleForTesting
        String createSolrUrl(String hostname, String port, String core) {
            return "http://" + hostname + ":" + port + "/solr/" + core;
        }
    }
}
