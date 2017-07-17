package com.flipkart.sherlock.semantic.core.search;

import com.flipkart.sherlock.semantic.common.dao.mysql.ConfigsDao;
import com.flipkart.sherlock.semantic.common.solr.FKHttpSolServer;
import com.flipkart.sherlock.semantic.common.solr.SolrServerProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Matchers;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by bharat.thakarar on 31/05/17.
 */

@RunWith(PowerMockRunner.class)
public class SolrSearchServerTest {
    @Mock
    private SolrServerProvider solrServerProviderMock;

    @Test
//    todo - add test cases
    public void query() throws Exception {
//        when(solrServerProviderMock.getSolrServer(eq("autosuggest"),eq("")))
//                .thenReturn(new FKHttpSolServer("http://10.32.1.253:25280/solr/autosuggest"));
//
//        SolrSearchServer solrSearchServer = new SolrSearchServer(solrServerProviderMock);
//
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.addParam(SearchRequest.Param.Q, "logged-qc-query_sstring:samsung");
//        SearchResponse searchResponse = solrSearchServer.query(searchRequest,"autosuggest", Collections.emptyMap());
//        System.out.println("searchResponse = " + searchResponse);

    }



}