package com.flipkart.sherlock.semantic.common.solr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.sherlock.semantic.common.dao.mysql.ConfigsDao;
import com.flipkart.sherlock.semantic.common.dao.mysql.entity.SolrEntities.*;
import com.flipkart.sherlock.semantic.common.solr.SolrUtilProvider.*;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Created by anurag.laddha on 26/04/17.
 */

@RunWith(PowerMockRunner.class)
public class SolrUtilProviderTest {

    @Mock
    ConfigsDao configsDaoMock;
    @Mock
    ExecutorService executorServiceMock;
    @Mock
    LoadingCache<String, Map<String, SolrFacetField>> solrFacetFieldMappingCacheMock;

    @Test
    public void testSetSolrFacetFieldMapping() throws Exception{

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> contextToFieldMap = ImmutableMap.of("context1", "solrF1Context1",     //value for context1
            "context2", "solrF1Context2");  //value for context2

        Map<String, SolrFacetField> solrFacetFieldMap = new HashMap<>();
        solrFacetFieldMap.put("f1", new SolrFacetField("f1", "solrF1", objectMapper.writeValueAsString(contextToFieldMap)));
        solrFacetFieldMap.put("f2", new SolrFacetField("f2", "solrF2", null));

        when(solrFacetFieldMappingCacheMock.get(anyString())).thenReturn(solrFacetFieldMap);
        SolrUtilProvider solrUtilProvider = new SolrUtilProvider(configsDaoMock, executorServiceMock);

        //swap internal loading cache with mocked cache - now loading cache will return what we have set it up with
        Whitebox.setInternalState(solrUtilProvider, "solrFacetFieldMappingCache", solrFacetFieldMappingCacheMock);

        //Validate facet field mapping
        Assert.assertEquals("solrF1", solrUtilProvider.getSolrFacetFieldMapping("f1"));
        Assert.assertEquals("solrF1Context1", solrUtilProvider.getSolrFacetFieldMapping("f1", "context1"));
        Assert.assertEquals("solrF1Context2", solrUtilProvider.getSolrFacetFieldMapping("f1", "context2"));
        Assert.assertEquals("solrF1", solrUtilProvider.getSolrFacetFieldMapping("f1", "nonExistentContext"));  //when context doesnt exist, use default field mapping
    }

    @Test
    public void testSolrFieldMappingLoader(){
        List<SolrFacetField> solrFacetFields = Lists.newArrayList(
            new SolrFacetField("f1", "solrF1", "context"),
            new SolrFacetField("f2", "solrF2", null));

        when(configsDaoMock.getAllSolrFacetFieldMappings()).thenReturn(solrFacetFields);

        SolrFacetFieldDataLoader dataLoader = new SolrFacetFieldDataLoader(configsDaoMock, executorServiceMock);
        Map<String, SolrFacetField> solrFacetFieldMap = dataLoader.getAllSolrFieldMappings();

        //Validate solr field mappings are getting created from rows returned from db
        Assert.assertTrue(solrFacetFieldMap.size() == 2);
        Assert.assertEquals("solrF1", solrFacetFieldMap.get("f1").getSolrField());
        Assert.assertEquals("context", solrFacetFieldMap.get("f1").getMetaData());
    }
}