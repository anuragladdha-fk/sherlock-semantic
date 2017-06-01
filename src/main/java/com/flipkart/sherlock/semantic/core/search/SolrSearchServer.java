package com.flipkart.sherlock.semantic.core.search;

import com.flipkart.sherlock.semantic.common.solr.SolrServerProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.collections.map.HashedMap;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.util.List;
import java.util.Map;

/**
 * Created by anurag.laddha on 23/05/17.
 */
@Singleton
public class SolrSearchServer implements ISearchEngine {
    private SolrServerProvider solrServerProvider;

    @Inject
    public SolrSearchServer(SolrServerProvider solrServerProvider) {
        this.solrServerProvider = solrServerProvider;
    }

    @Override
    public SearchResponse query(SearchRequest request, String collection, Map<String, String> params) {
        String experiment = "";
        if(params.containsKey("experiment"))
            experiment = params.get("experiment");
        SolrQuery solrQuery = searchReqToSolrReq(request);
        SolrServer solrServer = solrServerProvider.getSolrServer(collection,experiment);
        try {
            QueryResponse queryResponse = solrServer.query(solrQuery);
            return solrResToSearchResp(queryResponse);
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SearchResponse solrResToSearchResp(QueryResponse queryResponse) {
        SolrDocumentList solrDocs = queryResponse.getResults();
        SearchResponse searchResponse = new SearchResponse();
        if(solrDocs.size() > 0){
            for (SolrDocument solrDoc : solrDocs) {
                Map<String, Object> lazyDocMap = solrDoc.getFieldValueMap();
                Map<String, Object> docMap = new HashedMap();
                solrDoc.forEach(docMap::put);
                searchResponse.addDoc(docMap);
            }
            return searchResponse;
        }
        return null;
    }


    private SolrQuery searchReqToSolrReq(SearchRequest request) {
        SolrQuery solrQuery = new SolrQuery();
        Map<SearchRequest.Param, List<String>> origRequestParams = request.getRequestParams();
        origRequestParams.forEach((key,valueList) ->{
            valueList.forEach((v) -> {
                solrQuery.setParam(key.getParamName(),v);
            });
        });
        return solrQuery;
    }


}
