package com.flipkart.sherlock.semantic.core.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Created by anurag.laddha on 23/05/17.
 */
@AllArgsConstructor
@Getter
@ToString
public class SpellResponse {

    private String solrQuery;

    private List<String> results;

    public void addDoc(String suggestion) {
        results.add(suggestion);
    }

    public void addDocs(List<String> docs) {
        results.addAll(docs);
    }

}
