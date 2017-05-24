package com.flipkart.sherlock.semantic.common.solr;

import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;

/**
 * Created by anurag.laddha on 27/04/17.
 */

/**
 * Client to query to solr
 *
 * This client is thread-safe and you *MUST* re-use the same instance for all requests.
 * If instances are created on the fly, it can cause a connection leak.
 * The recommended practice is to keep a static instance of HttpSolrServer per solr server url and share it for all requests.
 */
public class FKHttpSolServer extends HttpSolrServer {

    private static final long serialVersionUID = 4981699383856525189L;

    public FKHttpSolServer(String baseURL) {
        super(baseURL);
    }

    //POST allows for longer queries to be made
    @Override
    public NamedList<Object> request(SolrRequest request) throws SolrServerException, IOException {
        request.setMethod(SolrRequest.METHOD.POST);
        return super.request(request);
    }

    //POST allows for longer queries to be made
    @Override
    public NamedList<Object> request(SolrRequest request, ResponseParser processor) throws SolrServerException, IOException {
        request.setMethod(SolrRequest.METHOD.POST);
        return super.request(request, processor);
    }
}