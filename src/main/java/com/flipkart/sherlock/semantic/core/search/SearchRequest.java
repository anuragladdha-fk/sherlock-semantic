package com.flipkart.sherlock.semantic.core.search;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.CommonParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anurag.laddha on 23/05/17.
 */
public class SearchRequest {

    public static enum Param {
        Q (CommonParams.Q),
        FQ (CommonParams.FQ);

        private final String paramName;

        private Param(String s) {
            paramName = s;
        }
    }

    private Map<Param, List<String>> requestParams = new HashMap<>();

    void addParam(Param param, String value){
        if (StringUtils.isBlank(value)){
            this.requestParams.remove(param);
        }
        else{
            if (this.requestParams.get(param) == null){
                this.requestParams.put(param, new ArrayList<>());
            }
            this.requestParams.get(param).add(value);
        }
    }

    void addParam(Param param, List<String> values){
        if (values != null && values.size() > 0){
            if (this.requestParams.get(param) == null){
                this.requestParams.put(param, new ArrayList<>());
            }
            values.forEach(val -> {
                if (StringUtils.isNotBlank(val)) {
                    this.requestParams.get(param).add(val);
                }
            });
        }
    }

    Map<Param, List<String>> getRequestParams(){
        Map<Param, List<String>> allRequstParams = new HashMap<>();
        allRequstParams.putAll(this.requestParams);
        return allRequstParams;
    }
}
