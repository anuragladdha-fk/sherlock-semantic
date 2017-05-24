package com.flipkart.sherlock.semantic.common.solr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flipkart.sherlock.semantic.common.config.SearchConfigProvider;
import com.flipkart.sherlock.semantic.common.util.SerDeUtils;
import com.flipkart.sherlock.semantic.common.solr.SolrServerProvider.*;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import junit.framework.Assert;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Created by anurag.laddha on 27/04/17.
 */

@RunWith(PowerMockRunner.class)
public class SolrServerProviderTest {

    @Mock
    SearchConfigProvider searchConfigProviderMock;
    @Mock
    ExecutorService executorServiceMock;
    @Mock
    LoadingCache<String, Map<ExperimentCore, FKHttpSolServer>> expToSolrServerMock;

    @Test
    public void testCoreExperimentDeserialisation() throws Exception{
        String serialisedStr = "[{\"experiment\":\"dd\",\"core\":{\"intentmeta\":\"intentmeta\"},\"host\":\"10.47.2.202\",\"port\":\"25280\",\"properties\":[]}]";
        List<Map<String,Object>> deserialiedList = SerDeUtils.castToGeneric(serialisedStr, new TypeReference<List<Map<String,Object>>>() {});
        Map<String, Object> experimentMap = deserialiedList.get(0);
        Assert.assertEquals("dd", experimentMap.get("experiment"));
        Assert.assertEquals("10.47.2.202", experimentMap.get("host"));
        Assert.assertEquals("25280", experimentMap.get("port"));
        Assert.assertEquals(ImmutableMap.of("intentmeta", "intentmeta"), (Map<String, String>)experimentMap.get("core"));
    }


    @Test
    public void testGetAllExperimentalSolrServers() throws Exception{
        /**
         * [{"experiment":"dd","core":{"intentmeta":"intentmeta", "meta":"meta"},"host":"10.47.2.202","port":"25280","properties":[]},
           {"experiment":"exp2","core":{"intentmeta":"abc", "meta":"abc"},"host":"10.47.2.202","port":"25280","properties":[]}]
         */

        String serialisedStr = "[{\"experiment\":\"dd\",\"core\":{\"intentmeta\":\"intentmeta\", \"meta\":\"meta\"},\"host\":\"10.47.2.202\",\"port\":\"25280\",\"properties\":[]},\n" +
            "{\"experiment\":\"exp2\",\"core\":{\"intentmeta\":\"abc\", \"meta\":\"abc\"},\"host\":\"10.47.2.202\",\"port\":\"25280\",\"properties\":[]}]";
        List<Map<String,Object>> deserialisedList = SerDeUtils.castToGeneric(serialisedStr, new TypeReference<List<Map<String,Object>>>() {});

        when(searchConfigProviderMock.getSearchConfig(eq("UtilCoreExperimentMapList"),
            Matchers.<TypeReference<List<Map<String,Object>>>>any())).thenReturn(deserialisedList);  //mock response

        ExperimentSolrCoreLoader experimentSolrCoreLoader = new ExperimentSolrCoreLoader(searchConfigProviderMock, executorServiceMock);
        Map<ExperimentCore, FKHttpSolServer> expToSolrServerMap = experimentSolrCoreLoader.getAllExperimentalSolrServers();

        //2 experiments and 2 cores per experiment --> 4 entries total
        Assert.assertEquals(4, expToSolrServerMap.size());

        //Validate the solr server url formed
        Assert.assertEquals("http://10.47.2.202:25280/solr/intentmeta",
            ((HttpSolrServer)expToSolrServerMap.get(new ExperimentCore("intentmeta", "dd"))).getBaseURL());
        Assert.assertEquals("http://10.47.2.202:25280/solr/abc",
                    ((HttpSolrServer)expToSolrServerMap.get(new ExperimentCore("intentmeta", "exp2"))).getBaseURL());
    }

    @Test
    public void testGetSolrServer(){
        Map<String, String> coreToUrlMap = ImmutableMap.of("core1", "http://abc/solr/core1",
            "core2", "http://abc/solr/core2");

        SolrServerProvider solrServerProvider = new SolrServerProvider(searchConfigProviderMock, executorServiceMock, coreToUrlMap);
        SolrServer solrServer = solrServerProvider.getSolrServer("core1", "abc"); //experiment "abc" should be ignored

        Assert.assertEquals("http://abc/solr/core1", ((HttpSolrServer)solrServer).getBaseURL());

        //Validate new instance is not created - cached solr server is given
        SolrServer solrServer2 = solrServerProvider.getSolrServer("core1", "abc");
        Assert.assertTrue(solrServer == solrServer2);
    }

    @Test
    public void testGetSolrServerWithExperiment() throws Exception {
        //Default core to url mapping
        Map<String, String> coreToUrlMap = ImmutableMap.of("core1", "http://abc/solr/core1",
            "core2", "http://abc/solr/core2");

        //Mapping of core to url based on experiment
        Map<ExperimentCore, FKHttpSolServer> expToSolrServerMap = new HashMap<>();
        expToSolrServerMap.put(new ExperimentCore("core1", "exp1"), new FKHttpSolServer("http://core1exp1/solr"));
        expToSolrServerMap.put(new ExperimentCore("core1", "exp2"), new FKHttpSolServer("http://core1exp2/solr"));

        when(searchConfigProviderMock.getSearchConfig(eq("TurnOnIntentExperiment"), eq(Boolean.class))).thenReturn(true);
        when(expToSolrServerMock.get(anyString())).thenReturn(expToSolrServerMap);

        SolrServerProvider solrServerProvider = new SolrServerProvider(searchConfigProviderMock, executorServiceMock, coreToUrlMap);

        //swap internal loading cache with mocked cache - now loading cache will return what we have set it up with
        Whitebox.setInternalState(solrServerProvider, "experimentCoreToSolrServerCache", expToSolrServerMock);

        SolrServer solrServer = solrServerProvider.getSolrServer("core1", "abc"); //no details on "core1" for experiment "abc' - should return default;
        Assert.assertEquals("http://abc/solr/core1", ((HttpSolrServer) solrServer).getBaseURL());

        SolrServer solrServer2 = solrServerProvider.getSolrServer("core1", null); //should return default
        Assert.assertEquals("http://abc/solr/core1", ((HttpSolrServer)solrServer2).getBaseURL());

        SolrServer solrServer3 = solrServerProvider.getSolrServer("core1", "exp1"); //should select core url based on experiment
        Assert.assertEquals("http://core1exp1/solr", ((HttpSolrServer)solrServer3).getBaseURL());

        //Validate new instance is not created - cached solr server is given
        Assert.assertTrue(solrServer == solrServer2);
    }
}