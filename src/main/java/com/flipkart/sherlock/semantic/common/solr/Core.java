package com.flipkart.sherlock.semantic.common.solr;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by dhruv.pancholi on 05/07/17.
 */
@Data
@AllArgsConstructor
public class Core {
    String hostname;
    int port;
    String core;

    public String getSolrUrl() {
        return "http://" + hostname + ":" + port + "/solr/" + core;
    }
}
