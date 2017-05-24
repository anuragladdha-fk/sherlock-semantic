package com.flipkart.sherlock.semantic.common.solr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flipkart.sherlock.semantic.common.config.Constants;
import com.flipkart.sherlock.semantic.common.dao.mysql.ConfigsDao;
import com.flipkart.sherlock.semantic.common.dao.mysql.entity.SolrEntities.*;
import com.flipkart.sherlock.semantic.common.util.SerDeUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by anurag.laddha on 26/04/17.
 */


@Singleton
public class SolrUtilProvider {
    private static final Logger log = LoggerFactory.getLogger(SolrUtilProvider.class);
    private static final TypeReference<Map<String, String>> strToStrTypeRef = new TypeReference<Map<String, String>>() {};

    private LoadingCache<String, Map<String, SolrFacetField>> solrFacetFieldMappingCache;

    @Inject
    public SolrUtilProvider(ConfigsDao configsDao,
                            @Named(Constants.GUICE_LOCAL_CACHE_LOADING_EXECUTOR_SERVICE) ExecutorService executorService) {
        SolrFacetFieldDataLoader solrFacetFieldDataLoader = new SolrFacetFieldDataLoader(configsDao, executorService);
        this.solrFacetFieldMappingCache = CacheBuilder.newBuilder().maximumSize(10).refreshAfterWrite(120, TimeUnit.SECONDS)
            .build(solrFacetFieldDataLoader);
    }


    /**
     * Provides solr field name for given facet name
     */
    public String getSolrFacetFieldMapping(String facetName){
        return getSolrFacetFieldMapping(facetName, null);
    }

    /**
     * Provides solr field name for given facet name and context
     */
    public String getSolrFacetFieldMapping(String facetName, String context){
        /**
         * Give solr field name for given facet name.
         * if context is provided, cast metadata associated with facet field name to Map and return the value associated with context, if any.
         */
        if (StringUtils.isNotBlank(facetName)){
            try {
                Map<String, SolrFacetField> allFacetFieldMappings = this.solrFacetFieldMappingCache.get(Constants.DUMMY_KEY);
                SolrFacetField facetField = allFacetFieldMappings.get(facetName);
                if (facetField != null){
                    String mappedField = facetField.getSolrField();
                    if (context != null && StringUtils.isNotBlank(facetField.getMetaData())){
                        Map<String, String> metaMap = SerDeUtils.castToGeneric(facetField.getMetaData(), strToStrTypeRef);
                        if(metaMap != null && metaMap.containsKey(context)){
                            mappedField = metaMap.get(context);
                        }
                    }
                    return mappedField;
                }
            }
            catch(Exception ex){
                log.error("Error in fetching facet field mappings from cache", ex);
            }
        }
        return null;
    }


    @AllArgsConstructor
    static class SolrFacetFieldDataLoader extends CacheLoader<String, Map<String, SolrFacetField>> {

        private ConfigsDao configsDao;
        private ExecutorService executorService;

        @Override
        public ListenableFuture<Map<String, SolrFacetField>> reload(String key, Map<String, SolrFacetField> oldValue) throws Exception {
            log.info("Queued solr facet field mapping async");
            ListenableFutureTask<Map<String, SolrFacetField>> task = ListenableFutureTask.create(() ->{
                log.info("Started solr facet field mappings async");
                Map<String, SolrFacetField> allFacetMappings = getAllSolrFieldMappings();
                log.info("Finished solr facet field mappings async. Number of elements : {}",
                    allFacetMappings != null ? allFacetMappings.size() : 0);
                return allFacetMappings;
            });
            this.executorService.submit(task);
            return task;
        }

        @Override
        public Map<String, SolrFacetField> load(String s) throws Exception {
            log.info("Fetching solr facet field mappings");
            Map<String, SolrFacetField> allFacetMappings = getAllSolrFieldMappings();
            log.info("Done fetching solr facet field mappings");
            return allFacetMappings;
        }

        Map<String, SolrFacetField> getAllSolrFieldMappings(){
            Map<String, SolrFacetField> allFacetMappings = new HashMap<>();
            try{
                List<SolrFacetField> facetFields = this.configsDao.getAllSolrFacetFieldMappings();
                if (facetFields != null && facetFields.size() > 0){
                    facetFields.forEach(f -> allFacetMappings.put(f.getField(), f));
                }
            }
            catch(Exception ex){
                log.error("Error in fetching solr facet field mappings", ex);
                //todo: metrics and alerts
            }
            return allFacetMappings;
        }
    }
}