package com.flipkart.sherlock.semantic.core.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by anurag.laddha on 23/05/17.
 */
@AllArgsConstructor
@Getter
public class SearchResponse {
    //todo add other required properties

    List<Map<String, Object>> results;

    public SearchResponse() {
        this.results = new ArrayList<>();
    }

    public void addDoc(Map<String, Object> doc){
        results.add(doc);
    }

    public void addDocs(List<Map<String, Object>> docs){
        results.addAll(docs);
    }

}
