package com.flipkart.sherlock.semantic.core.search;

import org.apache.solr.common.params.CommonParams;
import org.junit.Test;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by bharat.thakarar on 31/05/17.
 */
public class SearchRequestTest {
    @Test
    public void getRequestParams() throws Exception {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.addParam(SearchRequest.Param.FQ,"impressionsPerNumInputs:[9 TO *] OR impressions:[50 TO *]");
        searchRequest.addParam(SearchRequest.Param.FQ,"ctr:[0.03 TO *]");
        searchRequest.addParam(SearchRequest.Param.FQ,"query:samsung");
        Map<SearchRequest.Param, ArrayList<String>> reqParams = searchRequest.getRequestParams();
        System.out.println("reqParams = " + reqParams);
    }

}